package com.clinique.api.repository;

import com.clinique.api.model.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    Optional<Medecin> findByUserId(Long userId);

    Optional<Medecin> findByUserEmail(String email);

    List<Medecin> findBySpecialiteId(Long specialiteId);
}
