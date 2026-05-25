package com.clinique.api.service;

import com.clinique.api.dto.response.NotificationResponse;
import com.clinique.api.model.Notification;
import com.clinique.api.model.RendezVous;
import com.clinique.api.model.enums.StatutNotification;
import com.clinique.api.model.enums.TypeNotification;
import com.clinique.api.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd 'a' HH:mm");

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void notifierConfirmation(RendezVous rendezVous) {
        String contenu = "Bonjour " + rendezVous.getPatient().getPrenom()
                + ", votre rendez-vous du " + rendezVous.getDateHeureDebut().format(FORMAT)
                + " avec Dr. " + rendezVous.getMedecin().getPrenom() + " " + rendezVous.getMedecin().getNom()
                + " est confirme.";
        simuler(rendezVous, TypeNotification.EMAIL, contenu);
    }

    @Transactional
    public void notifierRappel(RendezVous rendezVous) {
        String contenu = "Rappel: vous avez un rendez-vous le " + rendezVous.getDateHeureDebut().format(FORMAT)
                + " avec Dr. " + rendezVous.getMedecin().getPrenom() + " " + rendezVous.getMedecin().getNom() + ".";
        simuler(rendezVous, TypeNotification.SMS, contenu);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> parRendezVous(Long rendezVousId) {
        return notificationRepository.findByRendezVousId(rendezVousId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    private void simuler(RendezVous rendezVous, TypeNotification type, String contenu) {
        String email = rendezVous.getPatient().getUser() != null
                ? rendezVous.getPatient().getUser().getEmail() : "inconnu";
        String destinataire = rendezVous.getPatient().getPrenom() + " " + rendezVous.getPatient().getNom();

        log.info("================ NOTIFICATION SIMULEE ================");
        log.info("Canal        : {}", type);
        log.info("Destinataire : {} <{}>", destinataire, email);
        log.info("Telephone    : {}", rendezVous.getPatient().getTelephone());
        log.info("Message      : {}", contenu);
        log.info("======================================================");

        Notification notification = new Notification();
        notification.setRendezVous(rendezVous);
        notification.setType(type);
        notification.setStatut(StatutNotification.ENVOYE);
        notification.setContenu(contenu);
        notification.setDateEnvoi(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
