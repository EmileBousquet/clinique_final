package com.clinique.api.service;

import com.clinique.api.dto.response.CreneauDisponibleResponse;
import com.clinique.api.exception.ConflitRendezVousException;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.model.HoraireTravail;
import com.clinique.api.model.Medecin;
import com.clinique.api.model.RendezVous;
import com.clinique.api.model.enums.StatutRendezVous;
import com.clinique.api.repository.HoraireTravailRepository;
import com.clinique.api.repository.MedecinRepository;
import com.clinique.api.repository.RendezVousRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibiliteService {

    private static final List<StatutRendezVous> STATUTS_ACTIFS =
            List.of(StatutRendezVous.PLANIFIE, StatutRendezVous.CONFIRME);

    private final MedecinRepository medecinRepository;
    private final HoraireTravailRepository horaireTravailRepository;
    private final RendezVousRepository rendezVousRepository;

    public DisponibiliteService(MedecinRepository medecinRepository,
                                HoraireTravailRepository horaireTravailRepository,
                                RendezVousRepository rendezVousRepository) {
        this.medecinRepository = medecinRepository;
        this.horaireTravailRepository = horaireTravailRepository;
        this.rendezVousRepository = rendezVousRepository;
    }

    @Transactional(readOnly = true)
    public List<CreneauDisponibleResponse> creneauxLibres(Long medecinId, LocalDate date) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new ResourceNotFoundException("Medecin", medecinId));

        DayOfWeek jour = date.getDayOfWeek();
        List<HoraireTravail> horaires = horaireTravailRepository.findByMedecinIdAndJourSemaine(medecinId, jour);
        if (horaires.isEmpty()) {
            return List.of();
        }

        int duree = medecin.getDureeConsultationMinutes();
        List<RendezVous> rendezVousDuJour = rendezVousActifsDuJour(medecinId, date);
        LocalDateTime maintenant = LocalDateTime.now();
        List<CreneauDisponibleResponse> creneaux = new ArrayList<>();

        for (HoraireTravail horaire : horaires) {
            LocalTime curseur = horaire.getHeureDebut();
            while (!curseur.plusMinutes(duree).isAfter(horaire.getHeureFin())) {
                LocalTime finCreneau = curseur.plusMinutes(duree);
                LocalDateTime debut = LocalDateTime.of(date, curseur);
                LocalDateTime fin = LocalDateTime.of(date, finCreneau);
                boolean libre = rendezVousDuJour.stream().noneMatch(rdv -> chevauche(debut, fin, rdv));
                if (debut.isAfter(maintenant) && libre) {
                    creneaux.add(new CreneauDisponibleResponse(curseur, finCreneau));
                }
                curseur = finCreneau;
            }
        }
        return creneaux;
    }

    @Transactional(readOnly = true)
    public void verifierCreneau(Medecin medecin, LocalDateTime debut, int dureeMinutes) {
        LocalDateTime fin = debut.plusMinutes(dureeMinutes);
        DayOfWeek jour = debut.getDayOfWeek();

        List<HoraireTravail> horaires =
                horaireTravailRepository.findByMedecinIdAndJourSemaine(medecin.getId(), jour);
        boolean dansHoraire = horaires.stream().anyMatch(horaire ->
                !debut.toLocalTime().isBefore(horaire.getHeureDebut())
                        && !fin.toLocalTime().isAfter(horaire.getHeureFin()));
        if (!dansHoraire) {
            throw new ConflitRendezVousException(
                    "Le creneau demande est en dehors des horaires de travail du medecin");
        }

        List<RendezVous> rendezVousDuJour = rendezVousActifsDuJour(medecin.getId(), debut.toLocalDate());
        boolean conflit = rendezVousDuJour.stream()
                .anyMatch(rdv -> debut.isBefore(rdv.getDateHeureFin()) && rdv.getDateHeureDebut().isBefore(fin));
        if (conflit) {
            throw new ConflitRendezVousException("Ce creneau est deja reserve pour ce medecin");
        }
    }

    private List<RendezVous> rendezVousActifsDuJour(Long medecinId, LocalDate date) {
        LocalDateTime debutJour = date.atStartOfDay();
        LocalDateTime finJour = date.plusDays(1).atStartOfDay();
        return rendezVousRepository.findByMedecinIdAndStatutInAndDateHeureDebutBetween(
                medecinId, STATUTS_ACTIFS, debutJour, finJour);
    }

    private boolean chevauche(LocalDateTime debut, LocalDateTime fin, RendezVous rdv) {
        return debut.isBefore(rdv.getDateHeureFin()) && rdv.getDateHeureDebut().isBefore(fin);
    }
}
