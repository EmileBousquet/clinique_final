package com.clinique.api.repository;

import com.clinique.api.model.DossierMedical;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {

    Optional<DossierMedical> findByPatientId(Long patientId);
}
