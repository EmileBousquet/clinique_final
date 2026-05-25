package com.clinique.api.service;

import com.clinique.api.dto.request.LoginRequest;
import com.clinique.api.dto.request.RegisterRequest;
import com.clinique.api.dto.response.AuthResponse;
import com.clinique.api.exception.RequeteInvalideException;
import com.clinique.api.model.DossierMedical;
import com.clinique.api.model.Patient;
import com.clinique.api.model.Role;
import com.clinique.api.model.User;
import com.clinique.api.repository.PatientRepository;
import com.clinique.api.repository.RoleRepository;
import com.clinique.api.repository.UserRepository;
import com.clinique.api.security.JwtService;
import com.clinique.api.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final String ROLE_PATIENT = "ROLE_PATIENT";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PatientRepository patientRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RequeteInvalideException("Un compte existe deja avec ce courriel");
        }

        Role rolePatient = roleRepository.findByNom(ROLE_PATIENT)
                .orElseThrow(() -> new IllegalStateException("Role " + ROLE_PATIENT + " absent"));

        User user = new User();
        user.setEmail(request.email());
        user.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        user.setActif(true);
        user.getRoles().add(rolePatient);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setNom(request.nom());
        patient.setPrenom(request.prenom());
        patient.setTelephone(request.telephone());
        patient.setDateNaissance(request.dateNaissance());
        patient.setNumeroDossier(genererNumeroDossier());

        DossierMedical dossier = new DossierMedical();
        dossier.setPatient(patient);
        patient.setDossier(dossier);

        userRepository.save(user);
        patientRepository.save(patient);

        return genererReponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse()));
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails.getUsername());
        return AuthResponse.bearer(
                token,
                jwtService.getExpirationMs() / 1000,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet()));
    }

    private AuthResponse genererReponse(User user) {
        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.bearer(
                token,
                jwtService.getExpirationMs() / 1000,
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getNom).collect(Collectors.toSet()));
    }

    private String genererNumeroDossier() {
        String numero;
        do {
            int suffixe = ThreadLocalRandom.current().nextInt(0, 100000);
            numero = "DOS-" + Year.now().getValue() + "-" + String.format("%05d", suffixe);
        } while (patientRepository.existsByNumeroDossier(numero));
        return numero;
    }
}
