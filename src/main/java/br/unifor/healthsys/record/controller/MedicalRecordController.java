package br.unifor.healthsys.record.controller;

import br.unifor.healthsys.record.dto.AtendimentoPayload;
import br.unifor.healthsys.record.model.MedicalRecord;
import br.unifor.healthsys.record.security.AuthenticatedUser;
import br.unifor.healthsys.record.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class MedicalRecordController {

    private final MedicalRecordService recordService;

    public MedicalRecordController(MedicalRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','ADMIN','PACIENTE')")
    public ResponseEntity<List<MedicalRecord>> findAll(@RequestParam(required = false) Long patientId,
                                                       @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser != null && "PACIENTE".equalsIgnoreCase(authenticatedUser.role()) && patientId == null) {
            throw new IllegalArgumentException("Paciente deve consultar o prontuario com patientId informado.");
        }
        if (authenticatedUser != null && "PACIENTE".equalsIgnoreCase(authenticatedUser.role())) {
            return ResponseEntity.ok(recordService.findAuthorizedByPatientId(patientId, authenticatedUser));
        }
        return ResponseEntity.ok(recordService.findAll(patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> create(@RequestBody MedicalRecord record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.create(record));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','ADMIN','PACIENTE')")
    public ResponseEntity<List<MedicalRecord>> findByPatientId(@PathVariable Long patientId,
                                                               @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser != null && "PACIENTE".equalsIgnoreCase(authenticatedUser.role())) {
            return ResponseEntity.ok(recordService.findAuthorizedByPatientId(patientId, authenticatedUser));
        }
        return ResponseEntity.ok(recordService.findByPatientId(patientId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','ADMIN','PACIENTE')")
    public ResponseEntity<MedicalRecord> findById(@PathVariable String id,
                                                  @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser != null && "PACIENTE".equalsIgnoreCase(authenticatedUser.role())) {
            return ResponseEntity.ok(recordService.findAuthorizedById(id, authenticatedUser));
        }
        return ResponseEntity.ok(recordService.findById(id));
    }

    @PostMapping("/{id}/entries")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> addEntry(@PathVariable String id,
                                                   @RequestBody MedicalRecord.RecordEntry entry,
                                                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(recordService.addEntry(id, entry, authenticatedUser));
    }

    @PostMapping("/{id}/prescriptions")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> addPrescription(@PathVariable String id,
                                                          @RequestBody MedicalRecord.Prescription prescription,
                                                          @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(recordService.addPrescription(id, prescription, authenticatedUser));
    }

    @PostMapping("/{id}/exams")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> addExam(@PathVariable String id,
                                                  @RequestBody MedicalRecord.Exam exam,
                                                  @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(recordService.addExam(id, exam, authenticatedUser));
    }

    @PostMapping("/{id}/atendimentos")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> registerAtendimento(@PathVariable String id,
                                                              @RequestBody AtendimentoPayload payload,
                                                              @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(recordService.registerAtendimento(id, payload, authenticatedUser));
    }

    @PatchMapping("/{id}/exams/{examId}/result")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> updateExamResult(@PathVariable String id,
                                                           @PathVariable String examId,
                                                           @RequestBody Map<String, Object> body) {
        String resultado = String.valueOf(body.getOrDefault("resultado", ""));
        MedicalRecord.StatusExame status = body.get("status") != null
                ? MedicalRecord.StatusExame.valueOf(String.valueOf(body.get("status")))
                : MedicalRecord.StatusExame.CONCLUIDO;
        return ResponseEntity.ok(recordService.updateExamResult(id, examId, resultado, status));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> update(@PathVariable String id,
                                                 @RequestBody MedicalRecord record) {
        return ResponseEntity.ok(recordService.update(id, record));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        recordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
