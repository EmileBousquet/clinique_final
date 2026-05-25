package com.clinique.api.controller;

import com.clinique.api.dto.response.NotificationResponse;
import com.clinique.api.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/rendez-vous/{rendezVousId}")
    public List<NotificationResponse> parRendezVous(@PathVariable Long rendezVousId) {
        return notificationService.parRendezVous(rendezVousId);
    }
}
