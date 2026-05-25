package com.clinique.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MedecinUpdateRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prenom est obligatoire")
        String prenom,

        @NotNull(message = "La specialite est obligatoire")
        Long specialiteId,

        @NotNull(message = "La duree de consultation est obligatoire")
        @Positive(message = "La duree de consultation doit etre positive")
        Integer dureeConsultationMinutes
) {
}
