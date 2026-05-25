package com.clinique.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecialiteRequest(

        @NotBlank(message = "Le nom de la specialite est obligatoire")
        @Size(max = 100, message = "Le nom ne peut depasser 100 caracteres")
        String nom,

        @Size(max = 255, message = "La description ne peut depasser 255 caracteres")
        String description
) {
}
