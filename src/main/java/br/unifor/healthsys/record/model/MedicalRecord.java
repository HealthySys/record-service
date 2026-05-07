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

    @Builder.Default
    private List<Prescription> prescriptions = new ArrayList<>();

    @Builder.Default
    private List<Exam> exams = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordEntry {
        private String type;        // CONSULTA, PROCEDIMENTO, TRIAGEM
        private String description;
        private String doctorId;
        private String doctorName;
        private LocalDateTime entryDate;
        private String origin;      // MANUAL, KAFKA_TRIAGE
        private String correlationId;
    }

    public enum Via {
        ORAL, INTRAVENOSA, INTRAMUSCULAR, SUBCUTANEA, TOPICA, INALATORIA, OUTRA
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prescription {
        private String id;
        private String medicamento;
        private String dosagem;
        private Via via;
        private String frequencia;
        private String duracao;
        private String observacoes;
        private String doctorId;
        private String doctorName;
        private LocalDateTime prescribedAt;
        private String correlationId;
    }

    public enum TipoExame {
        LABORATORIAL, IMAGEM, CARDIOLOGICO, OUTRO
    }

    public enum StatusExame {
        SOLICITADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exam {
        private String id;
        private TipoExame tipo;
        private String nome;
        private String indicacaoClinica;
        private StatusExame status;
        private String resultado;
        private String doctorId;
        private String doctorName;
        private LocalDateTime requestedAt;
        private LocalDateTime resultedAt;
        private String correlationId;
    }
}
