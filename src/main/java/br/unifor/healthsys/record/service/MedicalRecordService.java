package br.unifor.healthsys.record.service;

import br.unifor.healthsys.record.client.InternalPatientClient;
import br.unifor.healthsys.record.dto.AtendimentoPayload;
import br.unifor.healthsys.record.model.MedicalRecord;
import br.unifor.healthsys.record.security.AuthenticatedUser;
import br.unifor.healthsys.record.repository.MedicalRecordRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final InternalPatientClient internalPatientClient;

    public MedicalRecordService(MedicalRecordRepository recordRepository,
                                InternalPatientClient internalPatientClient) {
        this.recordRepository = recordRepository;
        this.internalPatientClient = internalPatientClient;
    }

    public MedicalRecord create(MedicalRecord record) {
        InternalPatientClient.InternalPatientSummaryResponse patient =
                internalPatientClient.fetchRequiredPatient(record.getPatientId());
        if (!patient.ativo()) {
            throw new IllegalArgumentException("Paciente inativo nao pode receber prontuario.");
        }
        if (record.getPatientName() == null || record.getPatientName().isBlank()) {
            record.setPatientName(patient.nome());
        }
        return recordRepository.save(record);
    }

    public List<MedicalRecord> findAll(Long patientId) {
        if (patientId != null) {
            return recordRepository.findByPatientId(patientId);
        }
        return recordRepository.findAll();
    }

    public List<MedicalRecord> findByPatientId(Long patientId) {
        return recordRepository.findByPatientId(patientId);
    }

    public List<MedicalRecord> findAuthorizedByPatientId(Long patientId, AuthenticatedUser authenticatedUser) {
        ensureOwnRecordAccess(patientId, authenticatedUser);
        return recordRepository.findByPatientId(patientId);
    }

    public MedicalRecord findById(String id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prontuario nao encontrado: " + id));
    }

    public MedicalRecord findAuthorizedById(String id, AuthenticatedUser authenticatedUser) {
        MedicalRecord record = findById(id);
        ensureOwnRecordAccess(record.getPatientId(), authenticatedUser);
        return record;
    }

    public MedicalRecord addEntry(String recordId, MedicalRecord.RecordEntry entry, AuthenticatedUser authenticatedUser) {
        MedicalRecord record = findById(recordId);
        entry.setEntryDate(LocalDateTime.now());
        entry.setOrigin("MANUAL");
        entry.setDoctorId(String.valueOf(authenticatedUser.userId()));
        entry.setDoctorName(authenticatedUser.nome());
        record.getEntries().add(entry);
        return recordRepository.save(record);
    }

    public MedicalRecord addPrescription(String recordId, MedicalRecord.Prescription prescription, AuthenticatedUser authenticatedUser) {
        MedicalRecord record = findById(recordId);
        prescription.setId(UUID.randomUUID().toString());
        prescription.setDoctorId(String.valueOf(authenticatedUser.userId()));
        prescription.setDoctorName(authenticatedUser.nome());
        prescription.setPrescribedAt(LocalDateTime.now());
        record.getPrescriptions().add(prescription);
        return recordRepository.save(record);
    }

    public MedicalRecord addExam(String recordId, MedicalRecord.Exam exam, AuthenticatedUser authenticatedUser) {
        MedicalRecord record = findById(recordId);
        exam.setId(UUID.randomUUID().toString());
        exam.setDoctorId(String.valueOf(authenticatedUser.userId()));
        exam.setDoctorName(authenticatedUser.nome());
        exam.setRequestedAt(LocalDateTime.now());
        if (exam.getStatus() == null) {
            exam.setStatus(MedicalRecord.StatusExame.SOLICITADO);
        }
        exam.setResultedAt(null);
        record.getExams().add(exam);
        return recordRepository.save(record);
    }

    public MedicalRecord registerAtendimento(String recordId, AtendimentoPayload payload, AuthenticatedUser authenticatedUser) {
        if (payload == null) {
            throw new IllegalArgumentException("Atendimento sem dados.");
        }

        String description = buildConsultationDescription(payload.consultation());
        List<AtendimentoPayload.PrescriptionInput> prescriptions = sanitizePrescriptions(payload.prescriptions());
        List<AtendimentoPayload.ExamInput> exams = sanitizeExams(payload.exams());

        if (description.isEmpty() && prescriptions.isEmpty() && exams.isEmpty()) {
            throw new IllegalArgumentException("Informe pelo menos uma seção do atendimento.");
        }

        MedicalRecord record = findById(recordId);
        String correlationId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        String doctorId = String.valueOf(authenticatedUser.userId());
        String doctorName = authenticatedUser.nome();

        if (!description.isEmpty()) {
            MedicalRecord.RecordEntry entry = MedicalRecord.RecordEntry.builder()
                    .type("CONSULTA")
                    .description(description)
                    .doctorId(doctorId)
                    .doctorName(doctorName)
                    .entryDate(now)
                    .origin("MANUAL")
                    .correlationId(correlationId)
                    .build();
            record.getEntries().add(entry);
        }

        for (AtendimentoPayload.PrescriptionInput input : prescriptions) {
            MedicalRecord.Prescription prescription = MedicalRecord.Prescription.builder()
                    .id(UUID.randomUUID().toString())
                    .medicamento(input.medicamento().trim())
                    .dosagem(input.dosagem().trim())
                    .via(input.via())
                    .frequencia(input.frequencia() == null ? "" : input.frequencia().trim())
                    .duracao(input.duracao() == null ? "" : input.duracao().trim())
                    .observacoes(input.observacoes() == null ? null : input.observacoes().trim())
                    .doctorId(doctorId)
                    .doctorName(doctorName)
                    .prescribedAt(now)
                    .correlationId(correlationId)
                    .build();
            record.getPrescriptions().add(prescription);
        }

        for (AtendimentoPayload.ExamInput input : exams) {
            MedicalRecord.Exam exam = MedicalRecord.Exam.builder()
                    .id(UUID.randomUUID().toString())
                    .tipo(input.tipo())
                    .nome(input.nome().trim())
                    .indicacaoClinica(input.indicacaoClinica() == null ? null : input.indicacaoClinica().trim())
                    .status(MedicalRecord.StatusExame.SOLICITADO)
                    .doctorId(doctorId)
                    .doctorName(doctorName)
                    .requestedAt(now)
                    .correlationId(correlationId)
                    .build();
            record.getExams().add(exam);
        }

        return recordRepository.save(record);
    }

    private String buildConsultationDescription(AtendimentoPayload.ConsultationInput input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        if (input.diagnosis() != null && !input.diagnosis().isBlank()) {
            sb.append("Diagnóstico: ").append(input.diagnosis().trim());
        }
        if (input.treatment() != null && !input.treatment().isBlank()) {
            if (sb.length() > 0) sb.append("\n\n");
            sb.append("Tratamento: ").append(input.treatment().trim());
        }
        if (input.observations() != null && !input.observations().isBlank()) {
            if (sb.length() > 0) sb.append("\n\n");
            sb.append("Observações: ").append(input.observations().trim());
        }
        return sb.toString();
    }

    private List<AtendimentoPayload.PrescriptionInput> sanitizePrescriptions(List<AtendimentoPayload.PrescriptionInput> inputs) {
        List<AtendimentoPayload.PrescriptionInput> result = new ArrayList<>();
        if (inputs == null) return result;
        for (AtendimentoPayload.PrescriptionInput input : inputs) {
            if (input == null) continue;
            if (input.medicamento() == null || input.medicamento().isBlank()) continue;
            if (input.dosagem() == null || input.dosagem().isBlank()) {
                throw new IllegalArgumentException("Dosagem obrigatória para a prescrição de " + input.medicamento().trim() + ".");
            }
            if (input.via() == null) {
                throw new IllegalArgumentException("Via de administração obrigatória para a prescrição de " + input.medicamento().trim() + ".");
            }
            result.add(input);
        }
        return result;
    }

    private List<AtendimentoPayload.ExamInput> sanitizeExams(List<AtendimentoPayload.ExamInput> inputs) {
        List<AtendimentoPayload.ExamInput> result = new ArrayList<>();
        if (inputs == null) return result;
        for (AtendimentoPayload.ExamInput input : inputs) {
            if (input == null) continue;
            if (input.nome() == null || input.nome().isBlank()) continue;
            if (input.tipo() == null) {
                throw new IllegalArgumentException("Tipo obrigatório para o exame " + input.nome().trim() + ".");
            }
            result.add(input);
        }
        return result;
    }

    public MedicalRecord updateExamResult(String recordId, String examId, String resultado, MedicalRecord.StatusExame status) {
        MedicalRecord record = findById(recordId);
        MedicalRecord.Exam target = record.getExams().stream()
                .filter(e -> examId.equals(e.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Exame nao encontrado: " + examId));
        target.setResultado(resultado);
        target.setStatus(status != null ? status : MedicalRecord.StatusExame.CONCLUIDO);
        target.setResultedAt(LocalDateTime.now());
        return recordRepository.save(record);
    }

    public MedicalRecord update(String id, MedicalRecord updated) {
        InternalPatientClient.InternalPatientSummaryResponse patient =
                internalPatientClient.fetchRequiredPatient(updated.getPatientId());
        if (!patient.ativo()) {
            throw new IllegalArgumentException("Paciente inativo nao pode ser associado ao prontuario.");
        }

        MedicalRecord existing = findById(id);
        existing.setPatientId(updated.getPatientId());
        existing.setPatientName(
                updated.getPatientName() == null || updated.getPatientName().isBlank()
                        ? patient.nome()
                        : updated.getPatientName()
        );
        existing.setDiagnosis(updated.getDiagnosis());
        existing.setTreatment(updated.getTreatment());
        existing.setObservations(updated.getObservations());
        existing.setResponsibleDoctorId(updated.getResponsibleDoctorId());
        existing.setResponsibleDoctorName(updated.getResponsibleDoctorName());
        return recordRepository.save(existing);
    }

    public void delete(String id) {
        recordRepository.delete(findById(id));
    }

    private void ensureOwnRecordAccess(Long patientId, AuthenticatedUser authenticatedUser) {
        if (patientId == null) {
            throw new AccessDeniedException("Prontuario sem paciente vinculado nao pode ser consultado pelo paciente.");
        }
        if (authenticatedUser == null || authenticatedUser.email() == null || authenticatedUser.email().isBlank()) {
            throw new AccessDeniedException("Sessao sem e-mail valido para validar acesso ao prontuario.");
        }

        InternalPatientClient.InternalPatientSummaryResponse patient =
                internalPatientClient.fetchRequiredPatient(patientId);
        if (patient.email() == null || !patient.email().equalsIgnoreCase(authenticatedUser.email())) {
            throw new AccessDeniedException("Paciente pode visualizar apenas o proprio prontuario.");
        }
    }
}
