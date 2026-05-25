package com.clinique.api.dto.response;

import com.clinique.api.model.RendezVous;
import com.clinique.api.model.enums.StatutRendezVous;

import java.time.LocalDateTime;

public record RendezVousResponse(
        Long id,
        Long medecinId,
        String medecinNom,
        Long patientId,
        String patientNom,
        LocalDateTime dateHeureDebut,
        LocalDateTime dateHeureFin,
        Integer dureeMinutes,
        StatutRendezVous statut,
        String motif
) {
    public static RendezVousResponse from(RendezVous rendezVous) {
        var medecin = rendezVous.getMedecin();
        var patient = rendezVous.getPatient();
        String medecinNom = "Dr. " + medecin.getPrenom() + " " + medecin.getNom();
        String patientNom = patient.getPrenom() + " " + patient.getNom();
        return new RendezVousResponse(
                rendezVous.getId(),
                medecin.getId(),
                medecinNom,
                patient.getId(),
                patientNom,
                rendezVous.getDateHeureDebut(),
                rendezVous.getDateHeureFin(),
                rendezVous.getDureeMinutes(),
                rendezVous.getStatut(),
                rendezVous.getMotif());
    }
}
