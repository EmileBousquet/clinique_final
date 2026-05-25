package com.clinique.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsultationRequest(

        @NotNull(message = "Le rendez-vous est obligatoire")
        Long rendezVousId,

        @NotBlank(message = "Les notes sont obligatoires")
        String notes,

        @NotBlank(message = "Le diagnostic est obligatoire")
        String diagnostic
) {
}
