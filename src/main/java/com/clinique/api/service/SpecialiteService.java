package com.clinique.api.service;

import com.clinique.api.dto.request.SpecialiteRequest;
import com.clinique.api.dto.response.SpecialiteResponse;
import com.clinique.api.exception.RequeteInvalideException;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.model.Specialite;
import com.clinique.api.repository.SpecialiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SpecialiteService {

    private final SpecialiteRepository specialiteRepository;

    public SpecialiteService(SpecialiteRepository specialiteRepository) {
        this.specialiteRepository = specialiteRepository;
    }

    @Transactional(readOnly = true)
    public List<SpecialiteResponse> findAll() {
        return specialiteRepository.findAll().stream()
                .map(SpecialiteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpecialiteResponse findById(Long id) {
        return SpecialiteResponse.from(getEntity(id));
    }

    public SpecialiteResponse create(SpecialiteRequest request) {
        if (specialiteRepository.existsByNom(request.nom())) {
            throw new RequeteInvalideException("Une specialite existe deja avec ce nom");
        }
        Specialite specialite = new Specialite(request.nom(), request.description());
        return SpecialiteResponse.from(specialiteRepository.save(specialite));
    }

    public SpecialiteResponse update(Long id, SpecialiteRequest request) {
        Specialite specialite = getEntity(id);
        specialite.setNom(request.nom());
        specialite.setDescription(request.description());
        return SpecialiteResponse.from(specialiteRepository.save(specialite));
    }

    public void delete(Long id) {
        Specialite specialite = getEntity(id);
        specialiteRepository.delete(specialite);
    }

    public Specialite getEntity(Long id) {
        return specialiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialite", id));
    }
}
