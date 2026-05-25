package com.clinique.api.service;

import com.clinique.api.dto.request.LigneOrdonnanceRequest;
import com.clinique.api.dto.request.OrdonnanceRequest;
import com.clinique.api.dto.response.OrdonnanceResponse;
import com.clinique.api.exception.AccesRefuseException;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.model.LigneOrdonnance;
import com.clinique.api.model.Medecin;
import com.clinique.api.model.Ordonnance;
import com.clinique.api.model.Patient;
import com.clinique.api.model.RendezVous;
import com.clinique.api.repository.OrdonnanceRepository;
import com.clinique.api.repository.RendezVousRepository;
import com.clinique.api.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrdonnanceService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MEDECIN = "ROLE_MEDECIN";
    private static final String ROLE_PATIENT = "ROLE_PATIENT";

    private final OrdonnanceRepository ordonnanceRepository;
    private final RendezVousRepository rendezVousRepository;
    private final MedecinService medecinService;
    private final PatientService patientService;

    public OrdonnanceService(OrdonnanceRepository ordonnanceRepository,
                             RendezVousRepository rendezVousRepository,
                             MedecinService medecinService,
                             PatientService patientService) {
        this.ordonnanceRepository = ordonnanceRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.medecinService = medecinService;
        this.patientService = patientService;
    }

    public OrdonnanceResponse creer(OrdonnanceRequest request) {
        RendezVous rendezVous = rendezVousRepository.findById(request.rendezVousId())
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous", request.rendezVousId()));

        Medecin medecin = medecinService.getMedecinCourant();
        if (!rendezVous.getMedecin().getId().equals(medecin.getId())) {
            throw new AccesRefuseException("Seul le medecin traitant peut rediger une ordonnance");
        }

        Ordonnance ordonnance = new Ordonnance();
        ordonnance.setRendezVous(rendezVous);
        ordonnance.setDureeValiditeJours(request.dureeValiditeJours());
        for (LigneOrdonnanceRequest ligne : request.lignes()) {
            LigneOrdonnance ligneOrdonnance = new LigneOrdonnance();
            ligneOrdonnance.setMedicament(ligne.medicament());
            ligneOrdonnance.setPosologie(ligne.posologie());
            ligneOrdonnance.setDureeTraitement(ligne.dureeTraitement());
            ordonnance.ajouterLigne(ligneOrdonnance);
        }

        return OrdonnanceResponse.from(ordonnanceRepository.save(ordonnance));
    }

    @Transactional(readOnly = true)
    public List<OrdonnanceResponse> mesOrdonnances() {
        if (SecurityUtils.hasRole(ROLE_PATIENT)) {
            Patient patient = patientService.getPatientCourant();
            return mapper(ordonnanceRepository.findByRendezVousPatientId(patient.getId()));
        }
        if (SecurityUtils.hasRole(ROLE_MEDECIN)) {
            Medecin medecin = medecinService.getMedecinCourant();
            return mapper(ordonnanceRepository.findByRendezVousMedecinId(medecin.getId()));
        }
        return mapper(ordonnanceRepository.findAll());
    }

    @Transactional(readOnly = true)
    public OrdonnanceResponse findById(Long id) {
        Ordonnance ordonnance = ordonnanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordonnance", id));
        autoriserLecture(ordonnance);
        return OrdonnanceResponse.from(ordonnance);
    }

    @Transactional(readOnly = true)
    public List<OrdonnanceResponse> parPatient(Long patientId) {
        return mapper(ordonnanceRepository.findByRendezVousPatientId(patientId));
    }

    private List<OrdonnanceResponse> mapper(List<Ordonnance> ordonnances) {
        return ordonnances.stream().map(OrdonnanceResponse::from).toList();
    }

    private void autoriserLecture(Ordonnance ordonnance) {
        if (SecurityUtils.hasRole(ROLE_ADMIN)) {
            return;
        }
        Long userId = SecurityUtils.getCurrentUserId();
        RendezVous rendezVous = ordonnance.getRendezVous();
        if (SecurityUtils.hasRole(ROLE_MEDECIN)
                && rendezVous.getMedecin().getUser().getId().equals(userId)) {
            return;
        }
        if (SecurityUtils.hasRole(ROLE_PATIENT)
                && rendezVous.getPatient().getUser().getId().equals(userId)) {
            return;
        }
        throw new AccesRefuseException("Acces refuse a cette ordonnance");
    }
}
