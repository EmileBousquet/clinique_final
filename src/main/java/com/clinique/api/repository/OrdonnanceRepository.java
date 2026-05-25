package com.clinique.api.repository;

import com.clinique.api.model.Ordonnance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Long> {

    List<Ordonnance> findByRendezVousPatientId(Long patientId);

    List<Ordonnance> findByRendezVousMedecinId(Long medecinId);
}
