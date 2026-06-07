package br.unifor.healthsys.record.controller;

import br.unifor.healthsys.record.dto.AtendimentoPayload;
import br.unifor.healthsys.record.model.MedicalRecord;
import br.unifor.healthsys.record.security.AuthenticatedUser;
import br.unifor.healthsys.record.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@Tag(name = "Prontuários", description = "Prontuário eletrônico: evoluções, prescrições, exames e atendimentos")
public class MedicalRecordController {

    private final MedicalRecordService recordService;

    public MedicalRecordController(MedicalRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    @Operation(summary = "Lista prontuários", description = "Opcionalmente filtra por patientId.")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','ADMIN')")
    public ResponseEntity<List<MedicalRecord>> findAll(@RequestParam(required = false) Long patientId) {
        return ResponseEntity.ok(recordService.findAll(patientId));
    }

    @PostMapping
    @Operation(summary = "Cria um prontuário", description = "Perfis: MEDICO, ADMIN.")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> create(@RequestBody MedicalRecord record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.create(record));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Prontuários de um paciente")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','ADMIN')")
    public ResponseEntity<List<MedicalRecord>> findByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(recordService.findByPatientId(patientId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca prontuário por ID")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','ADMIN')")
    public ResponseEntity<MedicalRecord> findById(@PathVariable String id) {
        return ResponseEntity.ok(recordService.findById(id));
    }

    @PostMapping("/{id}/entries")
    @Operation(summary = "Adiciona evolução ao prontuário")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> addEntry(@PathVariable String id,
                                                   @RequestBody MedicalRecord.RecordEntry entry,
                                                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(recordService.addEntry(id, entry, authenticatedUser));
    }

    @PostMapping("/{id}/prescriptions")
    @Operation(summary = "Adiciona prescrição ao prontuário")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> addPrescription(@PathVariable String id,
                                                          @RequestBody MedicalRecord.Prescription prescription,
                                                          @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(recordService.addPrescription(id, prescription, authenticatedUser));
    }

    @PostMapping("/{id}/exams")
    @Operation(summary = "Solicita exame no prontuário")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> addExam(@PathVariable String id,
                                                  @RequestBody MedicalRecord.Exam exam,
                                                  @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(recordService.addExam(id, exam, authenticatedUser));
    }

    @PostMapping("/{id}/atendimentos")
    @Operation(summary = "Registra um atendimento", description = "Persiste diagnóstico/tratamento e pode finalizar o atendimento.")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> registerAtendimento(@PathVariable String id,
                                                              @RequestBody AtendimentoPayload payload,
                                                              @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(recordService.registerAtendimento(id, payload, authenticatedUser));
    }

    @PatchMapping("/{id}/exams/{examId}/result")
    @Operation(summary = "Atualiza resultado de exame", description = "Informa o resultado e o status (default CONCLUIDO) de um exame.")
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
    @Operation(summary = "Atualiza um prontuário")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<MedicalRecord> update(@PathVariable String id,
                                                 @RequestBody MedicalRecord record) {
        return ResponseEntity.ok(recordService.update(id, record));
    }

}
