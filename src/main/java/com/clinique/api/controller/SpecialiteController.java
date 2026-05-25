package com.clinique.api.controller;

import com.clinique.api.dto.request.SpecialiteRequest;
import com.clinique.api.dto.response.SpecialiteResponse;
import com.clinique.api.service.SpecialiteService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/specialites")
public class SpecialiteController {

    private final SpecialiteService specialiteService;

    public SpecialiteController(SpecialiteService specialiteService) {
        this.specialiteService = specialiteService;
    }

    @GetMapping
    public List<SpecialiteResponse> findAll() {
        return specialiteService.findAll();
    }

    @GetMapping("/{id}")
    public SpecialiteResponse findById(@PathVariable Long id) {
        return specialiteService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteResponse> create(@Valid @RequestBody SpecialiteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specialiteService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SpecialiteResponse update(@PathVariable Long id, @Valid @RequestBody SpecialiteRequest request) {
        return specialiteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        specialiteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
