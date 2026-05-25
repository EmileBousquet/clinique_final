package com.clinique.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Le courriel est obligatoire")
        @Email(message = "Le courriel n'est pas valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse
) {
}
