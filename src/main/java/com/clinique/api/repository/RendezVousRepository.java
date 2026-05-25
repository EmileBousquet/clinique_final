package com.clinique.api.repository;

import com.clinique.api.model.RendezVous;
import com.clinique.api.model.enums.StatutRendezVous;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    List<RendezVous> findByMedecinIdOrderByDateHeureDebutAsc(Long medecinId);

    List<RendezVous> findByPatientIdOrderByDateHeureDebutDesc(Long patientId);

    List<RendezVous> findByMedecinIdAndStatutInAndDateHeureDebutBetween(
            Long medecinId,
            Collection<StatutRendezVous> statuts,
            LocalDateTime debut,
            LocalDateTime fin);

    List<RendezVous> findByStatutInAndDateHeureDebutBetween(
            Collection<StatutRendezVous> statuts,
            LocalDateTime debut,
            LocalDateTime fin);
}
