package com.clinique.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LigneOrdonnanceRequest(

        @NotBlank(message = "Le medicament est obligatoire")
        @Size(max = 150, message = "Le medicament ne peut depasser 150 caracteres")
        String medicament,

        @NotBlank(message = "La posologie est obligatoire")
        @Size(max = 255, message = "La posologie ne peut depasser 255 caracteres")
        String posologie,

        @Size(max = 100, message = "La duree de traitement ne peut depasser 100 caracteres")
        String dureeTraitement
) {
}
