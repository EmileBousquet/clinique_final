package com.clinique.api.controller;

import com.clinique.api.dto.request.HoraireTravailRequest;
import com.clinique.api.dto.request.MedecinRequest;
import com.clinique.api.dto.request.MedecinUpdateRequest;
import com.clinique.api.dto.response.CreneauDisponibleResponse;
import com.clinique.api.dto.response.HoraireTravailResponse;
import com.clinique.api.dto.response.MedecinResponse;
import com.clinique.api.service.DisponibiliteService;
import com.clinique.api.service.MedecinService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medecins")
public class MedecinController {

    private final MedecinService medecinService;
    private final DisponibiliteService disponibiliteService;

    public MedecinController(MedecinService medecinService, DisponibiliteService disponibiliteService) {
        this.medecinService = medecinService;
        this.disponibiliteService = disponibiliteService;
    }

    @GetMapping
    public List<MedecinResponse> findAll() {
        return medecinService.findAll();
    }

    @GetMapping("/{id}")
    public MedecinResponse findById(@PathVariable Long id) {
        return medecinService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedecinResponse> create(@Valid @RequestBody MedecinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medecinService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MedecinResponse update(@PathVariable Long id, @Valid @RequestBody MedecinUpdateRequest request) {
        return medecinService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medecinService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/horaires")
    public List<HoraireTravailResponse> getHoraires(@PathVariable Long id) {
        return medecinService.getHoraires(id);
    }

    @PutMapping("/{id}/horaires")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public List<HoraireTravailResponse> remplacerHoraires(@PathVariable Long id,
                                                          @Valid @RequestBody List<HoraireTravailRequest> horaires) {
        return medecinService.remplacerHoraires(id, horaires);
    }

    @GetMapping("/{id}/disponibilites")
    public List<CreneauDisponibleResponse> disponibilites(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return disponibiliteService.creneauxLibres(id, date);
    }
}
