package com.clinique.api.repository;

import com.clinique.api.model.HoraireTravail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface HoraireTravailRepository extends JpaRepository<HoraireTravail, Long> {

    List<HoraireTravail> findByMedecinIdAndJourSemaine(Long medecinId, DayOfWeek jourSemaine);

    List<HoraireTravail> findByMedecinId(Long medecinId);
}
