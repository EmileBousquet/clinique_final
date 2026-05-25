package com.clinique.api.service;

import com.clinique.api.dto.request.RendezVousRequest;
import com.clinique.api.dto.response.RendezVousResponse;
import com.clinique.api.exception.AccesRefuseException;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.model.Medecin;
import com.clinique.api.model.Patient;
import com.clinique.api.model.RendezVous;
import com.clinique.api.model.enums.StatutRendezVous;
import com.clinique.api.repository.RendezVousRepository;
import com.clinique.api.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RendezVousService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MEDECIN = "ROLE_MEDECIN";
    private static final String ROLE_PATIENT = "ROLE_PATIENT";

    private final RendezVousRepository rendezVousRepository;
    private final MedecinService medecinService;
    private final PatientService patientService;
    private final DisponibiliteService disponibiliteService;
    private final NotificationService notificationService;

    public RendezVousService(RendezVousRepository rendezVousRepository,
                             MedecinService medecinService,
                             PatientService patientService,
                             DisponibiliteService disponibiliteService,
                             NotificationService notificationService) {
        this.rendezVousRepository = rendezVousRepository;
        this.medecinService = medecinService;
        this.patientService = patientService;
        this.disponibiliteService = disponibiliteService;
        this.notificationService = notificationService;
    }

    public RendezVousResponse prendre(RendezVousRequest request) {
        Patient patient = patientService.getPatientCourant();
        Medecin medecin = medecinService.getEntity(request.medecinId());
        int duree = medecin.getDureeConsultationMinutes();

        disponibiliteService.verifierCreneau(medecin, request.dateHeureDebut(), duree);

        RendezVous rendezVous = new RendezVous();
        rendezVous.setMedecin(medecin);
        rendezVous.setPatient(patient);
        rendezVous.setDateHeureDebut(request.dateHeureDebut());
        rendezVous.setDureeMinutes(duree);
        rendezVous.setStatut(StatutRendezVous.PLANIFIE);
        rendezVous.setMotif(request.motif());

        return RendezVousResponse.from(rendezVousRepository.save(rendezVous));
    }

    public RendezVousResponse changerStatut(Long id, StatutRendezVous statut) {
        RendezVous rendezVous = getEntity(id);
        autoriserModificationStatut(rendezVous, statut);
        rendezVous.setStatut(statut);
        rendezVousRepository.save(rendezVous);
        if (statut == StatutRendezVous.CONFIRME) {
            notificationService.notifierConfirmation(rendezVous);
        }
        return RendezVousResponse.from(rendezVous);
    }

    @Transactional(readOnly = true)
    public List<RendezVousResponse> tous() {
        return rendezVousRepository.findAll().stream()
                .map(RendezVousResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RendezVousResponse> mesRendezVousPatient() {
        Patient patient = patientService.getPatientCourant();
        return rendezVousRepository.findByPatientIdOrderByDateHeureDebutDesc(patient.getId()).stream()
                .map(RendezVousResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RendezVousResponse> mesRendezVousMedecin() {
        Medecin medecin = medecinService.getMedecinCourant();
        return rendezVousRepository.findByMedecinIdOrderByDateHeureDebutAsc(medecin.getId()).stream()
                .map(RendezVousResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RendezVousResponse findById(Long id) {
        RendezVous rendezVous = getEntity(id);
        autoriserLecture(rendezVous);
        return RendezVousResponse.from(rendezVous);
    }

    public RendezVous getEntity(Long id) {
        return rendezVousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous", id));
    }

    private void autoriserModificationStatut(RendezVous rendezVous, StatutRendezVous statut) {
        if (SecurityUtils.hasRole(ROLE_ADMIN)) {
            return;
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (SecurityUtils.hasRole(ROLE_MEDECIN)) {
            if (!rendezVous.getMedecin().getUser().getId().equals(userId)) {
                throw new AccesRefuseException("Ce rendez-vous ne vous appartient pas");
            }
            return;
        }
        if (SecurityUtils.hasRole(ROLE_PATIENT)) {
            if (!rendezVous.getPatient().getUser().getId().equals(userId)) {
                throw new AccesRefuseException("Ce rendez-vous ne vous appartient pas");
            }
            if (statut != StatutRendezVous.ANNULE) {
                throw new AccesRefuseException("Un patient ne peut qu'annuler son rendez-vous");
            }
            return;
        }
        throw new AccesRefuseException("Action non autorisee");
    }

    private void autoriserLecture(RendezVous rendezVous) {
        if (SecurityUtils.hasRole(ROLE_ADMIN)) {
            return;
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (SecurityUtils.hasRole(ROLE_MEDECIN)
                && rendezVous.getMedecin().getUser().getId().equals(userId)) {
            return;
        }
        if (SecurityUtils.hasRole(ROLE_PATIENT)
                && rendezVous.getPatient().getUser().getId().equals(userId)) {
            return;
        }
        throw new AccesRefuseException("Acces refuse a ce rendez-vous");
    }
}
