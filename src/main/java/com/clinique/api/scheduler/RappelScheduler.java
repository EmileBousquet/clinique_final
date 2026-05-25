package com.clinique.api.scheduler;

import com.clinique.api.model.RendezVous;
import com.clinique.api.model.enums.StatutRendezVous;
import com.clinique.api.repository.RendezVousRepository;
import com.clinique.api.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RappelScheduler {

    private static final Logger log = LoggerFactory.getLogger(RappelScheduler.class);
    private static final List<StatutRendezVous> STATUTS_ACTIFS =
            List.of(StatutRendezVous.PLANIFIE, StatutRendezVous.CONFIRME);

    private final RendezVousRepository rendezVousRepository;
    private final NotificationService notificationService;
    private final int heuresAvant;

    public RappelScheduler(RendezVousRepository rendezVousRepository,
                           NotificationService notificationService,
                           @Value("${app.notification.rappel-heures-avant}") int heuresAvant) {
        this.rendezVousRepository = rendezVousRepository;
        this.notificationService = notificationService;
        this.heuresAvant = heuresAvant;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void envoyerRappels() {
        LocalDateTime debutFenetre = LocalDateTime.now().plusHours(heuresAvant);
        LocalDateTime finFenetre = debutFenetre.plusHours(1);

        List<RendezVous> aRappeler = rendezVousRepository
                .findByStatutInAndDateHeureDebutBetween(STATUTS_ACTIFS, debutFenetre, finFenetre);

        if (aRappeler.isEmpty()) {
            return;
        }

        log.info("Envoi de {} rappel(s) pour les rendez-vous dans {} heures", aRappeler.size(), heuresAvant);
        aRappeler.forEach(notificationService::notifierRappel);
    }
}
