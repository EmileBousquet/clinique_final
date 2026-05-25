package com.clinique.api.config;

import com.clinique.api.model.DossierMedical;
import com.clinique.api.model.HoraireTravail;
import com.clinique.api.model.Medecin;
import com.clinique.api.model.Patient;
import com.clinique.api.model.Role;
import com.clinique.api.model.Specialite;
import com.clinique.api.model.User;
import com.clinique.api.repository.MedecinRepository;
import com.clinique.api.repository.PatientRepository;
import com.clinique.api.repository.RoleRepository;
import com.clinique.api.repository.SpecialiteRepository;
import com.clinique.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SpecialiteRepository specialiteRepository;
    private final MedecinRepository medecinRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           SpecialiteRepository specialiteRepository,
                           MedecinRepository medecinRepository,
                           PatientRepository patientRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.specialiteRepository = specialiteRepository;
        this.medecinRepository = medecinRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role roleAdmin = creerRoleSiAbsent("ROLE_ADMIN");
        Role roleMedecin = creerRoleSiAbsent("ROLE_MEDECIN");
        Role rolePatient = creerRoleSiAbsent("ROLE_PATIENT");

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setMotDePasse(passwordEncoder.encode(adminPassword));
            admin.setActif(true);
            admin.getRoles().add(roleAdmin);
            userRepository.save(admin);
            log.info("Compte administrateur cree: {}", adminEmail);
        }

        if (specialiteRepository.count() == 0) {
            specialiteRepository.saveAll(List.of(
                    new Specialite("Generaliste", "Medecine generale"),
                    new Specialite("Cardiologie", "Maladies du coeur et des vaisseaux"),
                    new Specialite("Pediatrie", "Suivi medical des enfants"),
                    new Specialite("Dermatologie", "Maladies de la peau")));
            log.info("Specialites initialisees");
        }

        if (medecinRepository.count() == 0) {
            creerMedecin(roleMedecin, "Bousquet", "Emile", "emile.bousquet@clinique.com");
        }

        if (patientRepository.count() == 0) {
            creerPatient(rolePatient, "Fretzer", "Marc", "marc.fretzer@mail.com", "514-555-0001", "DOS-2026-00001");
            creerPatient(rolePatient, "Jean", "Sophie", "sophie.jean@mail.com", "514-555-0002", "DOS-2026-00002");
            creerPatient(rolePatient, "Paul", "Luc", "luc.paul@mail.com", "514-555-0003", "DOS-2026-00003");
            log.info("Patients par defaut initialises");
        }
    }

    private void creerMedecin(Role roleMedecin, String nom, String prenom, String email) {
        Specialite generaliste = specialiteRepository.findByNom("Generaliste").orElse(null);

        User user = new User();
        user.setEmail(email);
        user.setMotDePasse(passwordEncoder.encode("Medecin@123"));
        user.setActif(true);
        user.getRoles().add(roleMedecin);

        Medecin medecin = new Medecin();
        medecin.setUser(user);
        medecin.setNom(nom);
        medecin.setPrenom(prenom);
        medecin.setSpecialite(generaliste);
        medecin.setDureeConsultationMinutes(30);

        List<DayOfWeek> jours = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        for (DayOfWeek jour : jours) {
            medecin.getHoraires().add(horaire(medecin, jour, LocalTime.of(9, 0), LocalTime.of(12, 0)));
            medecin.getHoraires().add(horaire(medecin, jour, LocalTime.of(13, 0), LocalTime.of(17, 0)));
        }

        userRepository.save(user);
        medecinRepository.save(medecin);
        log.info("Medecin par defaut cree: Dr. {} {}", prenom, nom);
    }

    private HoraireTravail horaire(Medecin medecin, DayOfWeek jour, LocalTime debut, LocalTime fin) {
        HoraireTravail horaire = new HoraireTravail();
        horaire.setMedecin(medecin);
        horaire.setJourSemaine(jour);
        horaire.setHeureDebut(debut);
        horaire.setHeureFin(fin);
        return horaire;
    }

    private void creerPatient(Role rolePatient, String nom, String prenom, String email,
                              String telephone, String numeroDossier) {
        User user = new User();
        user.setEmail(email);
        user.setMotDePasse(passwordEncoder.encode("Patient@123"));
        user.setActif(true);
        user.getRoles().add(rolePatient);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setNom(nom);
        patient.setPrenom(prenom);
        patient.setTelephone(telephone);
        patient.setDateNaissance(LocalDate.of(1990, 1, 1));
        patient.setNumeroDossier(numeroDossier);

        DossierMedical dossier = new DossierMedical();
        dossier.setPatient(patient);
        patient.setDossier(dossier);

        userRepository.save(user);
        patientRepository.save(patient);
    }

    private Role creerRoleSiAbsent(String nom) {
        return roleRepository.findByNom(nom)
                .orElseGet(() -> roleRepository.save(new Role(nom)));
    }
}
