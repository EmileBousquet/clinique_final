package com.clinique.api.service;

import com.clinique.api.dto.request.ConsultationRequest;
import com.clinique.api.dto.response.ConsultationResponse;
import com.clinique.api.exception.AccesRefuseException;
import com.clinique.api.exception.RequeteInvalideException;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.model.Consultation;
import com.clinique.api.model.DossierMedical;
import com.clinique.api.model.Medecin;
import com.clinique.api.model.Patient;
import com.clinique.api.model.RendezVous;
import com.clinique.api.model.enums.StatutRendezVous;
import com.clinique.api.repository.ConsultationRepository;
import com.clinique.api.repository.DossierMedicalRepository;
import com.clinique.api.repository.RendezVousRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final RendezVousRepository rendezVousRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final MedecinService medecinService;

    public ConsultationService(ConsultationRepository consultationRepository,
                               RendezVousRepository rendezVousRepository,
                               DossierMedicalRepository dossierMedicalRepository,
                               MedecinService medecinService) {
        this.consultationRepository = consultationRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.dossierMedicalRepository = dossierMedicalRepository;
        this.medecinService = medecinService;
    }

    public ConsultationResponse creer(ConsultationRequest request) {
        RendezVous rendezVous = rendezVousRepository.findById(request.rendezVousId())
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous", request.rendezVousId()));

        Medecin medecin = medecinService.getMedecinCourant();
        if (!rendezVous.getMedecin().getId().equals(medecin.getId())) {
            throw new AccesRefuseException("Vous n'etes pas le medecin traitant de ce rendez-vous");
        }
        if (consultationRepository.findByRendezVousId(rendezVous.getId()).isPresent()) {
            throw new RequeteInvalideException("Une consultation existe deja pour ce rendez-vous");
        }

        DossierMedical dossier = resoudreDossier(rendezVous.getPatient());

        Consultation consultation = new Consultation();
        consultation.setRendezVous(rendezVous);
        consultation.setDossier(dossier);
        consultation.setNotes(request.notes());
        consultation.setDiagnostic(request.diagnostic());
        consultationRepository.save(consultation);

        rendezVous.setStatut(StatutRendezVous.TERMINE);
        rendezVousRepository.save(rendezVous);

        return ConsultationResponse.from(consultation);
    }

    private DossierMedical resoudreDossier(Patient patient) {
        if (patient.getDossier() != null) {
            return patient.getDossier();
        }
        return dossierMedicalRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    DossierMedical dossier = new DossierMedical();
                    dossier.setPatient(patient);
                    return dossierMedicalRepository.save(dossier);
                });
    }
}
