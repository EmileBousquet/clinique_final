package com.clinique.api.dto.response;

import com.clinique.api.model.LigneOrdonnance;

public record LigneOrdonnanceResponse(
        Long id,
        String medicament,
        String posologie,
        String dureeTraitement
) {
    public static LigneOrdonnanceResponse from(LigneOrdonnance ligne) {
        return new LigneOrdonnanceResponse(
                ligne.getId(),
                ligne.getMedicament(),
                ligne.getPosologie(),
                ligne.getDureeTraitement());
    }
}
