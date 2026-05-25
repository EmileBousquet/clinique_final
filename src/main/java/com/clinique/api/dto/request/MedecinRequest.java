package com.clinique.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MedecinRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prenom est obligatoire")
        String prenom,

        @NotBlank(message = "Le courriel est obligatoire")
        @Email(message = "Le courriel n'est pas valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caracteres")
        String motDePasse,

        @NotNull(message = "La specialite est obligatoire")
        Long specialiteId,

        @NotNull(message = "La duree de consultation est obligatoire")
        @Positive(message = "La duree de consultation doit etre positive")
        Integer dureeConsultationMinutes,

        @Valid
        List<HoraireTravailRequest> horaires
) {
}
