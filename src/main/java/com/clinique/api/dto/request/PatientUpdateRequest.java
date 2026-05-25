package com.clinique.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientUpdateRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prenom est obligatoire")
        String prenom,

        @Size(max = 20, message = "Le telephone ne peut depasser 20 caracteres")
        String telephone,

        @Past(message = "La date de naissance doit etre dans le passe")
        LocalDate dateNaissance
) {
}
