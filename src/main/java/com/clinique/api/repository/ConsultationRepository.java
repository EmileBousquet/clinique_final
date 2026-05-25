package com.clinique.api.repository;

import com.clinique.api.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    Optional<Consultation> findByRendezVousId(Long rendezVousId);

    List<Consultation> findByDossierIdOrderByDateConsultationDesc(Long dossierId);
}
