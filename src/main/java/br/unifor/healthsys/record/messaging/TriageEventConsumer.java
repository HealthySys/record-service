package br.unifor.healthsys.record.messaging;

import br.unifor.healthsys.record.model.MedicalRecord;
import br.unifor.healthsys.record.model.ProcessedEvent;
import br.unifor.healthsys.record.repository.MedicalRecordRepository;
import br.unifor.healthsys.record.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TriageEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TriageEventConsumer.class);

    private final MedicalRecordRepository recordRepository;
    private final ProcessedEventRepository processedEventRepository;

    public TriageEventConsumer(MedicalRecordRepository recordRepository,
                               ProcessedEventRepository processedEventRepository) {
        this.recordRepository = recordRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "triagem-events", groupId = "record-service-group")
    public void consumeTriageEvent(JsonNode event) {
        String type = event.path("type").asText("TRIAGEM_CLASSIFICADA");
        if (!"TRIAGEM_CLASSIFICADA".equals(type)) {
            log.debug("Evento ignorado pelo prontuario. type={}", type);
            return;
        }

        String correlationId = event.path("correlationId").asText("");
        if (!correlationId.isBlank() && processedEventRepository.existsByCorrelationId(correlationId)) {
            log.warn("Evento duplicado descartado no prontuario. correlationId={}", correlationId);
            return;
        }

        Long patientId = event.get("patientId").asLong();
        String patientName = event.get("patientName").asText();

        MedicalRecord record = recordRepository
                .findTopByPatientIdOrderByCreatedAtDesc(patientId)
                .orElseGet(() -> MedicalRecord.builder()
                        .patientId(patientId)
                        .patientName(patientName)
                        .build());

        MedicalRecord.RecordEntry entry = MedicalRecord.RecordEntry.builder()
                .type("TRIAGEM")
                .description(event.path("observations").asText("")
                        + " | classificacao=" + event.path("riskClassification").asText(""))
                .entryDate(LocalDateTime.now())
                .origin("KAFKA_TRIAGE")
                .correlationId(correlationId)
                .build();

        record.getEntries().add(entry);
        recordRepository.save(record);
        if (!correlationId.isBlank()) {
            processedEventRepository.save(ProcessedEvent.builder()
                    .id(correlationId)
                    .correlationId(correlationId)
                    .processedAt(LocalDateTime.now())
                    .build());
        }

        log.info("Prontuario atualizado com evento de triagem para paciente ID: {}", patientId);
    }
}
