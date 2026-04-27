package br.unifor.healthsys.record.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "medical_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    private String id;

    private Long patientId;

    private String patientName;

    private String diagnosis;

    private String treatment;

    private String observations;

    private String responsibleDoctorId;

    private String responsibleDoctorName;

    @Builder.Default
    private List<RecordEntry> entries = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordEntry {
        private String type;        // CONSULTA, EXAME, PROCEDIMENTO, TRIAGEM
        private String description;
        private String doctorId;
        private String doctorName;
        private LocalDateTime entryDate;
        private String origin;      // MANUAL, KAFKA_TRIAGE, KAFKA_EXAME
        private String correlationId;
    }
}
