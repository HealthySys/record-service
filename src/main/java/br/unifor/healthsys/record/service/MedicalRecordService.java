package br.unifor.healthsys.record.service;

import br.unifor.healthsys.record.model.MedicalRecord;
import br.unifor.healthsys.record.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;

    public MedicalRecordService(MedicalRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public MedicalRecord create(MedicalRecord record) {
        return recordRepository.save(record);
    }

    public List<MedicalRecord> findByPatientId(Long patientId) {
        return recordRepository.findByPatientId(patientId);
    }

    public MedicalRecord findById(String id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prontuario nao encontrado: " + id));
    }

    public MedicalRecord addEntry(String recordId, MedicalRecord.RecordEntry entry) {
        MedicalRecord record = findById(recordId);
        entry.setEntryDate(LocalDateTime.now());
        entry.setOrigin("MANUAL");
        record.getEntries().add(entry);
        return recordRepository.save(record);
    }

    public MedicalRecord update(String id, MedicalRecord updated) {
        MedicalRecord existing = findById(id);
        existing.setDiagnosis(updated.getDiagnosis());
        existing.setTreatment(updated.getTreatment());
        existing.setObservations(updated.getObservations());
        return recordRepository.save(existing);
    }
}
