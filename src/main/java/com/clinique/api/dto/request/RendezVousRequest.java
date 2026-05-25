package com.clinique.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RendezVousRequest(

        @NotNull(message = "Le medecin est obligatoire")
        Long medecinId,

        @NotNull(message = "La date et l'heure du rendez-vous sont obligatoires")
        @Future(message = "Le rendez-vous doit etre dans le futur")
        LocalDateTime dateHeureDebut,

        @Size(max = 255, message = "Le motif ne peut depasser 255 caracteres")
        String motif
) {
}
