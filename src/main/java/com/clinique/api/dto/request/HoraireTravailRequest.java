package com.clinique.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HoraireTravailRequest(

        @NotNull(message = "Le jour de la semaine est obligatoire")
        DayOfWeek jourSemaine,

        @NotNull(message = "L'heure de debut est obligatoire")
        LocalTime heureDebut,

        @NotNull(message = "L'heure de fin est obligatoire")
        LocalTime heureFin
) {
}
