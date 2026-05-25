package com.clinique.api.controller;

import com.clinique.api.dto.request.PatientUpdateRequest;
import com.clinique.api.dto.response.PatientResponse;
import com.clinique.api.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public List<PatientResponse> findAll() {
        return patientService.findAll();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponse monProfil() {
        return patientService.monProfil();
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponse updateMonProfil(@Valid @RequestBody PatientUpdateRequest request) {
        return patientService.updateMonProfil(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public PatientResponse findById(@PathVariable Long id) {
        return patientService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponse update(@PathVariable Long id, @Valid @RequestBody PatientUpdateRequest request) {
        return patientService.update(id, request);
    }
}
