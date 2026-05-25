package com.clinique.api.controller;

import com.clinique.api.dto.request.ConsultationRequest;
import com.clinique.api.dto.response.ConsultationResponse;
import com.clinique.api.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultations")
@PreAuthorize("hasRole('MEDECIN')")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping
    public ResponseEntity<ConsultationResponse> creer(@Valid @RequestBody ConsultationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.creer(request));
    }
}
