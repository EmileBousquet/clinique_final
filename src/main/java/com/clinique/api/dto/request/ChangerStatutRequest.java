package com.clinique.api.dto.request;

import com.clinique.api.model.enums.StatutRendezVous;
import jakarta.validation.constraints.NotNull;

public record ChangerStatutRequest(

        @NotNull(message = "Le statut est obligatoire")
        StatutRendezVous statut
) {
}
