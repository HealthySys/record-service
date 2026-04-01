package br.unifor.healthsys.record.messaging;

import br.unifor.healthsys.record.model.MedicalRecord;
import br.unifor.healthsys.record.repository.MedicalRecordRepository;
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

    public TriageEventConsumer(MedicalRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @KafkaListener(topics = "healthsys.triage.events", groupId = "record-service-group")
    public void consumeTriageEvent(JsonNode event) {
        log.info("Evento de triagem recebido para paciente ID: {}", event.get("patientId"));

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
                .description(event.get("observations").asText(""))
                .entryDate(LocalDateTime.now())
                .origin("KAFKA_TRIAGE")
                .build();

        record.getEntries().add(entry);
        recordRepository.save(record);

        log.info("Prontuario atualizado com evento de triagem para paciente ID: {}", patientId);
    }
}
