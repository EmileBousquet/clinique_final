package com.clinique.api.service;

import com.clinique.api.dto.request.PatientUpdateRequest;
import com.clinique.api.dto.response.PatientResponse;
import com.clinique.api.exception.AccesRefuseException;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.model.Patient;
import com.clinique.api.repository.PatientRepository;
import com.clinique.api.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> findAll() {
        return patientRepository.findAll().stream()
                .map(PatientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(Long id) {
        return PatientResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PatientResponse monProfil() {
        return PatientResponse.from(getPatientCourant());
    }

    public PatientResponse update(Long id, PatientUpdateRequest request) {
        Patient patient = getEntity(id);
        patient.setNom(request.nom());
        patient.setPrenom(request.prenom());
        patient.setTelephone(request.telephone());
        patient.setDateNaissance(request.dateNaissance());
        return PatientResponse.from(patientRepository.save(patient));
    }

    public PatientResponse updateMonProfil(PatientUpdateRequest request) {
        Patient patient = getPatientCourant();
        patient.setNom(request.nom());
        patient.setPrenom(request.prenom());
        patient.setTelephone(request.telephone());
        patient.setDateNaissance(request.dateNaissance());
        return PatientResponse.from(patientRepository.save(patient));
    }

    public Patient getEntity(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    public Patient getPatientCourant() {
        Long userId = SecurityUtils.getCurrentUserId();
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new AccesRefuseException("Aucun profil patient associe au compte courant"));
    }
}
