package com.clinique.api.repository;

import com.clinique.api.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    Optional<Patient> findByUserEmail(String email);

    boolean existsByNumeroDossier(String numeroDossier);
}
