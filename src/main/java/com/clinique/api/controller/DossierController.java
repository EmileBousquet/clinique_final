package com.clinique.api.controller;

import com.clinique.api.dto.response.DossierResponse;
import com.clinique.api.service.DossierService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dossiers")
public class DossierController {

    private final DossierService dossierService;

    public DossierController(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    @GetMapping("/mon-dossier")
    @PreAuthorize("hasRole('PATIENT')")
    public DossierResponse monDossier() {
        return dossierService.monDossier();
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public DossierResponse parPatient(@PathVariable Long patientId) {
        return dossierService.parPatient(patientId);
    }
}
