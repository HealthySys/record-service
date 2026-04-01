package br.unifor.healthsys.record.repository;

import br.unifor.healthsys.record.model.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {

    List<MedicalRecord> findByPatientId(Long patientId);

    Optional<MedicalRecord> findTopByPatientIdOrderByCreatedAtDesc(Long patientId);
}
