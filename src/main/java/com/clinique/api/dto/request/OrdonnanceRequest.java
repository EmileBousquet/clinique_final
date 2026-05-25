package com.clinique.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrdonnanceRequest(

        @NotNull(message = "Le rendez-vous est obligatoire")
        Long rendezVousId,

        @NotNull(message = "La duree de validite est obligatoire")
        @Positive(message = "La duree de validite doit etre positive")
        Integer dureeValiditeJours,

        @Valid
        @NotEmpty(message = "L'ordonnance doit contenir au moins un medicament")
        List<LigneOrdonnanceRequest> lignes
) {
}
