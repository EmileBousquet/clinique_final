package com.clinique.api.repository;

import com.clinique.api.model.Specialite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialiteRepository extends JpaRepository<Specialite, Long> {

    Optional<Specialite> findByNom(String nom);

    boolean existsByNom(String nom);
}
