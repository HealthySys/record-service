package br.unifor.healthsys.record.service;

import br.unifor.healthsys.record.client.InternalPatientClient;
import br.unifor.healthsys.record.model.MedicalRecord;
import br.unifor.healthsys.record.security.AuthenticatedUser;
import br.unifor.healthsys.record.repository.MedicalRecordRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public MedicalRecord addEntry(String recordId, MedicalRecord.RecordEntry entry) {
        MedicalRecord record = findById(recordId);
        entry.setEntryDate(LocalDateTime.now());
        entry.setOrigin("MANUAL");
        record.getEntries().add(entry);
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
