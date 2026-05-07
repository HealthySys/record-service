package br.unifor.healthsys.record.dto;

import br.unifor.healthsys.record.model.MedicalRecord;

import java.util.List;

public record AtendimentoPayload(
        ConsultationInput consultation,
        List<PrescriptionInput> prescriptions,
        List<ExamInput> exams
) {

    public record ConsultationInput(
            String diagnosis,
            String treatment,
            String observations
    ) {
    }

    public record PrescriptionInput(
            String medicamento,
            String dosagem,
            MedicalRecord.Via via,
            String frequencia,
            String duracao,
            String observacoes
    ) {
    }

    public record ExamInput(
            MedicalRecord.TipoExame tipo,
            String nome,
            String indicacaoClinica
    ) {
    }
}
