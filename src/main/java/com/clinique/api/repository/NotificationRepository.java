package com.clinique.api.repository;

import com.clinique.api.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRendezVousId(Long rendezVousId);
}
