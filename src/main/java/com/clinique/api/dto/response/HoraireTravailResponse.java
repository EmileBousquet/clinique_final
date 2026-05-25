package com.clinique.api.dto.response;

import com.clinique.api.model.HoraireTravail;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HoraireTravailResponse(
        Long id,
        DayOfWeek jourSemaine,
        LocalTime heureDebut,
        LocalTime heureFin
) {
    public static HoraireTravailResponse from(HoraireTravail horaire) {
        return new HoraireTravailResponse(
                horaire.getId(),
                horaire.getJourSemaine(),
                horaire.getHeureDebut(),
                horaire.getHeureFin());
    }
}
