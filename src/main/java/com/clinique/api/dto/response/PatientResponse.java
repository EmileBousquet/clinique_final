package com.clinique.api.dto.response;

import com.clinique.api.model.Patient;

import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String nom,
        String prenom,
        String telephone,
        LocalDate dateNaissance,
        String numeroDossier,
        String email
) {
    public static PatientResponse from(Patient patient) {
        String email = patient.getUser() != null ? patient.getUser().getEmail() : null;
        return new PatientResponse(
                patient.getId(),
                patient.getNom(),
                patient.getPrenom(),
                patient.getTelephone(),
                patient.getDateNaissance(),
                patient.getNumeroDossier(),
                email);
    }
}
