package com.clinique.api.dto.response;

import com.clinique.api.model.Notification;
import com.clinique.api.model.enums.StatutNotification;
import com.clinique.api.model.enums.TypeNotification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long rendezVousId,
        TypeNotification type,
        StatutNotification statut,
        String contenu,
        LocalDateTime dateEnvoi
) {
    public static NotificationResponse from(Notification notification) {
        Long rendezVousId = notification.getRendezVous() != null ? notification.getRendezVous().getId() : null;
        return new NotificationResponse(
                notification.getId(),
                rendezVousId,
                notification.getType(),
                notification.getStatut(),
                notification.getContenu(),
                notification.getDateEnvoi());
    }
}
