package com.clinique.api.dto.response;

import com.clinique.api.model.DossierMedical;

import java.time.LocalDateTime;
import java.util.List;

public record DossierResponse(
        Long id,
        Long patientId,
        String patientNom,
        String numeroDossier,
        LocalDateTime dateCreation,
        List<ConsultationResponse> consultations
) {
    public static DossierResponse from(DossierMedical dossier) {
        var patient = dossier.getPatient();
        String patientNom = patient.getPrenom() + " " + patient.getNom();
        List<ConsultationResponse> consultations = dossier.getConsultations().stream()
                .map(ConsultationResponse::from)
                .toList();
        return new DossierResponse(
                dossier.getId(),
                patient.getId(),
                patientNom,
                patient.getNumeroDossier(),
                dossier.getDateCreation(),
                consultations);
    }
}
