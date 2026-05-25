package com.clinique.api.dto.response;

import com.clinique.api.model.Medecin;

import java.util.List;

public record MedecinResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        Long specialiteId,
        String specialiteNom,
        Integer dureeConsultationMinutes,
        List<HoraireTravailResponse> horaires
) {
    public static MedecinResponse from(Medecin medecin) {
        Long specialiteId = medecin.getSpecialite() != null ? medecin.getSpecialite().getId() : null;
        String specialiteNom = medecin.getSpecialite() != null ? medecin.getSpecialite().getNom() : null;
        String email = medecin.getUser() != null ? medecin.getUser().getEmail() : null;
        List<HoraireTravailResponse> horaires = medecin.getHoraires().stream()
                .map(HoraireTravailResponse::from)
                .toList();
        return new MedecinResponse(
                medecin.getId(),
                medecin.getNom(),
                medecin.getPrenom(),
                email,
                specialiteId,
                specialiteNom,
                medecin.getDureeConsultationMinutes(),
                horaires);
    }
}
