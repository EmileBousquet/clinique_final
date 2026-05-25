package com.clinique.api.controller;

import com.clinique.api.dto.request.ChangerStatutRequest;
import com.clinique.api.dto.request.RendezVousRequest;
import com.clinique.api.dto.response.RendezVousResponse;
import com.clinique.api.service.RendezVousService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rendez-vous")
public class RendezVousController {

    private final RendezVousService rendezVousService;

    public RendezVousController(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<RendezVousResponse> prendre(@Valid @RequestBody RendezVousRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rendezVousService.prendre(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RendezVousResponse> tous() {
        return rendezVousService.tous();
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public List<RendezVousResponse> mesRendezVousPatient() {
        return rendezVousService.mesRendezVousPatient();
    }

    @GetMapping("/medecin")
    @PreAuthorize("hasRole('MEDECIN')")
    public List<RendezVousResponse> mesRendezVousMedecin() {
        return rendezVousService.mesRendezVousMedecin();
    }

    @GetMapping("/{id}")
    public RendezVousResponse findById(@PathVariable Long id) {
        return rendezVousService.findById(id);
    }

    @PatchMapping("/{id}/statut")
    public RendezVousResponse changerStatut(@PathVariable Long id,
                                            @Valid @RequestBody ChangerStatutRequest request) {
        return rendezVousService.changerStatut(id, request.statut());
    }
}
