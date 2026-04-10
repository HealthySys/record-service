package br.unifor.healthsys.record.controller;

import br.unifor.healthsys.record.model.MedicalRecord;
import br.unifor.healthsys.record.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class MedicalRecordController {

    private final MedicalRecordService recordService;

    public MedicalRecordController(MedicalRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public ResponseEntity<List<MedicalRecord>> findAll() {
        return ResponseEntity.ok(recordService.findAll());
    }

    @PostMapping
    public ResponseEntity<MedicalRecord> create(@RequestBody MedicalRecord record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.create(record));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecord>> findByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(recordService.findByPatientId(patientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecord> findById(@PathVariable String id) {
        return ResponseEntity.ok(recordService.findById(id));
    }

    @PostMapping("/{id}/entries")
    public ResponseEntity<MedicalRecord> addEntry(@PathVariable String id,
                                                   @RequestBody MedicalRecord.RecordEntry entry) {
        return ResponseEntity.ok(recordService.addEntry(id, entry));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecord> update(@PathVariable String id,
                                                 @RequestBody MedicalRecord record) {
        return ResponseEntity.ok(recordService.update(id, record));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        recordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
