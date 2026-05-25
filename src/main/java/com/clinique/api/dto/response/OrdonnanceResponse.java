package com.clinique.api.dto.response;

import com.clinique.api.model.Ordonnance;

import java.time.LocalDate;
import java.util.List;

public record OrdonnanceResponse(
        Long id,
        Long rendezVousId,
        Long medecinId,
        String medecinNom,
        Long patientId,
        LocalDate dateEmission,
        Integer dureeValiditeJours,
        List<LigneOrdonnanceResponse> lignes
) {
    public static OrdonnanceResponse from(Ordonnance ordonnance) {
        var rendezVous = ordonnance.getRendezVous();
        var medecin = rendezVous.getMedecin();
        var patient = rendezVous.getPatient();
        String medecinNom = "Dr. " + medecin.getPrenom() + " " + medecin.getNom();
        List<LigneOrdonnanceResponse> lignes = ordonnance.getLignes().stream()
                .map(LigneOrdonnanceResponse::from)
                .toList();
        return new OrdonnanceResponse(
                ordonnance.getId(),
                rendezVous.getId(),
                medecin.getId(),
                medecinNom,
                patient.getId(),
                ordonnance.getDateEmission(),
                ordonnance.getDureeValiditeJours(),
                lignes);
    }
}
