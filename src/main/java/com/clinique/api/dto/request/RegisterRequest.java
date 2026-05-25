package com.clinique.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(

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

        @Size(max = 20, message = "Le telephone ne peut depasser 20 caracteres")
        String telephone,

        @Past(message = "La date de naissance doit etre dans le passe")
        LocalDate dateNaissance
) {
}
