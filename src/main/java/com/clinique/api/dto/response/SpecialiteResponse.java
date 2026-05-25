package com.clinique.api.dto.response;

import com.clinique.api.model.Specialite;

public record SpecialiteResponse(
        Long id,
        String nom,
        String description
) {
    public static SpecialiteResponse from(Specialite specialite) {
        return new SpecialiteResponse(
                specialite.getId(),
                specialite.getNom(),
                specialite.getDescription());
    }
}
