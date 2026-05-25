package com.clinique.api.service;

import com.clinique.api.dto.request.HoraireTravailRequest;
import com.clinique.api.dto.request.MedecinRequest;
import com.clinique.api.dto.request.MedecinUpdateRequest;
import com.clinique.api.dto.response.HoraireTravailResponse;
import com.clinique.api.dto.response.MedecinResponse;
import com.clinique.api.exception.AccesRefuseException;
import com.clinique.api.exception.RequeteInvalideException;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.model.HoraireTravail;
import com.clinique.api.model.Medecin;
import com.clinique.api.model.Role;
import com.clinique.api.model.Specialite;
import com.clinique.api.model.User;
import com.clinique.api.repository.MedecinRepository;
import com.clinique.api.repository.RoleRepository;
import com.clinique.api.repository.SpecialiteRepository;
import com.clinique.api.repository.UserRepository;
import com.clinique.api.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MedecinService {

    private static final String ROLE_MEDECIN = "ROLE_MEDECIN";

    private final MedecinRepository medecinRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SpecialiteRepository specialiteRepository;
    private final PasswordEncoder passwordEncoder;

    public MedecinService(MedecinRepository medecinRepository,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          SpecialiteRepository specialiteRepository,
                          PasswordEncoder passwordEncoder) {
        this.medecinRepository = medecinRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.specialiteRepository = specialiteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<MedecinResponse> findAll() {
        return medecinRepository.findAll().stream()
                .map(MedecinResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedecinResponse findById(Long id) {
        return MedecinResponse.from(getEntity(id));
    }

    public MedecinResponse create(MedecinRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RequeteInvalideException("Un compte existe deja avec ce courriel");
        }
        Specialite specialite = specialiteRepository.findById(request.specialiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialite", request.specialiteId()));
        Role roleMedecin = roleRepository.findByNom(ROLE_MEDECIN)
                .orElseThrow(() -> new IllegalStateException("Role " + ROLE_MEDECIN + " absent"));

        User user = new User();
        user.setEmail(request.email());
        user.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        user.setActif(true);
        user.getRoles().add(roleMedecin);

        Medecin medecin = new Medecin();
        medecin.setUser(user);
        medecin.setNom(request.nom());
        medecin.setPrenom(request.prenom());
        medecin.setSpecialite(specialite);
        medecin.setDureeConsultationMinutes(request.dureeConsultationMinutes());

        if (request.horaires() != null) {
            for (HoraireTravailRequest horaire : request.horaires()) {
                medecin.getHoraires().add(construireHoraire(medecin, horaire));
            }
        }

        userRepository.save(user);
        return MedecinResponse.from(medecinRepository.save(medecin));
    }

    public MedecinResponse update(Long id, MedecinUpdateRequest request) {
        Medecin medecin = getEntity(id);
        Specialite specialite = specialiteRepository.findById(request.specialiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialite", request.specialiteId()));
        medecin.setNom(request.nom());
        medecin.setPrenom(request.prenom());
        medecin.setSpecialite(specialite);
        medecin.setDureeConsultationMinutes(request.dureeConsultationMinutes());
        return MedecinResponse.from(medecinRepository.save(medecin));
    }

    public void delete(Long id) {
        Medecin medecin = getEntity(id);
        User user = medecin.getUser();
        medecinRepository.delete(medecin);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Transactional(readOnly = true)
    public List<HoraireTravailResponse> getHoraires(Long medecinId) {
        return getEntity(medecinId).getHoraires().stream()
                .map(HoraireTravailResponse::from)
                .toList();
    }

    public List<HoraireTravailResponse> remplacerHoraires(Long medecinId, List<HoraireTravailRequest> horaires) {
        Medecin medecin = getEntity(medecinId);
        verifierProprietaireOuAdmin(medecin);
        medecin.getHoraires().clear();
        for (HoraireTravailRequest horaire : horaires) {
            medecin.getHoraires().add(construireHoraire(medecin, horaire));
        }
        medecinRepository.save(medecin);
        return medecin.getHoraires().stream()
                .map(HoraireTravailResponse::from)
                .toList();
    }

    public Medecin getEntity(Long id) {
        return medecinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medecin", id));
    }

    public Medecin getMedecinCourant() {
        Long userId = SecurityUtils.getCurrentUserId();
        return medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new AccesRefuseException("Aucun profil medecin associe au compte courant"));
    }

    private HoraireTravail construireHoraire(Medecin medecin, HoraireTravailRequest request) {
        if (!request.heureDebut().isBefore(request.heureFin())) {
            throw new RequeteInvalideException("L'heure de debut doit preceder l'heure de fin");
        }
        HoraireTravail horaire = new HoraireTravail();
        horaire.setMedecin(medecin);
        horaire.setJourSemaine(request.jourSemaine());
        horaire.setHeureDebut(request.heureDebut());
        horaire.setHeureFin(request.heureFin());
        return horaire;
    }

    private void verifierProprietaireOuAdmin(Medecin medecin) {
        if (SecurityUtils.hasRole("ROLE_ADMIN")) {
            return;
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (medecin.getUser() == null || !medecin.getUser().getId().equals(userId)) {
            throw new AccesRefuseException("Vous ne pouvez gerer que vos propres horaires");
        }
    }
}
