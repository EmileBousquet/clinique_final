package com.clinique.api.service;

import com.clinique.api.dto.response.DossierResponse;
import com.clinique.api.exception.AccesRefuseException;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.model.DossierMedical;
import com.clinique.api.model.Patient;
import com.clinique.api.repository.DossierMedicalRepository;
import com.clinique.api.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DossierService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MEDECIN = "ROLE_MEDECIN";
    private static final String ROLE_PATIENT = "ROLE_PATIENT";

    private final DossierMedicalRepository dossierMedicalRepository;
    private final PatientService patientService;

    public DossierService(DossierMedicalRepository dossierMedicalRepository,
                          PatientService patientService) {
        this.dossierMedicalRepository = dossierMedicalRepository;
        this.patientService = patientService;
    }

    public DossierResponse parPatient(Long patientId) {
        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier medical du patient", patientId));

        if (SecurityUtils.hasRole(ROLE_PATIENT)
                && !SecurityUtils.hasRole(ROLE_ADMIN)
                && !SecurityUtils.hasRole(ROLE_MEDECIN)) {
            Patient courant = patientService.getPatientCourant();
            if (!courant.getId().equals(patientId)) {
                throw new AccesRefuseException("Vous ne pouvez consulter que votre propre dossier");
            }
        }
        return DossierResponse.from(dossier);
    }

    public DossierResponse monDossier() {
        Patient patient = patientService.getPatientCourant();
        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier medical du patient", patient.getId()));
        return DossierResponse.from(dossier);
    }
}
