package com.clinique.api.dto.response;

import com.clinique.api.model.Consultation;

import java.time.LocalDateTime;

public record ConsultationResponse(
        Long id,
        Long rendezVousId,
        LocalDateTime dateConsultation,
        String notes,
        String diagnostic,
        Long medecinId,
        String medecinNom
) {
    public static ConsultationResponse from(Consultation consultation) {
        var rendezVous = consultation.getRendezVous();
        var medecin = rendezVous.getMedecin();
        String medecinNom = "Dr. " + medecin.getPrenom() + " " + medecin.getNom();
        return new ConsultationResponse(
                consultation.getId(),
                rendezVous.getId(),
                consultation.getDateConsultation(),
                consultation.getNotes(),
                consultation.getDiagnostic(),
                medecin.getId(),
                medecinNom);
    }
}
