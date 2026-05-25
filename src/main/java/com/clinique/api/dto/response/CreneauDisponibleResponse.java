package com.clinique.api.dto.response;

import java.time.LocalTime;

public record CreneauDisponibleResponse(
        LocalTime heureDebut,
        LocalTime heureFin
) {
}
