# Conception — API Gestion de Clinique Médicale

---

## 1. Diagramme de base de données

```mermaid
erDiagram
    USERS ||--o{ USERS_ROLES : possede
    ROLES ||--o{ USERS_ROLES : attribue
    USERS ||--o| MEDECINS : "profil (1-1)"
    USERS ||--o| PATIENTS : "profil (1-1)"
    SPECIALITES ||--o{ MEDECINS : "classe"
    MEDECINS ||--o{ HORAIRES_TRAVAIL : "definit"
    MEDECINS ||--o{ RENDEZ_VOUS : "recoit"
    PATIENTS ||--o{ RENDEZ_VOUS : "prend"
    PATIENTS ||--|| DOSSIERS_MEDICAUX : "possede"
    DOSSIERS_MEDICAUX ||--o{ CONSULTATIONS : "regroupe"
    RENDEZ_VOUS ||--o| CONSULTATIONS : "donne lieu a"
    RENDEZ_VOUS ||--o{ ORDONNANCES : "genere"
    ORDONNANCES ||--o{ LIGNES_ORDONNANCE : "contient"
    RENDEZ_VOUS ||--o{ NOTIFICATIONS : "declenche"

    USERS {
        bigint id PK
        varchar email UK "login, unique"
        varchar mot_de_passe "hash BCrypt"
        boolean actif
        datetime date_creation
    }
    ROLES {
        bigint id PK
        varchar nom UK "ROLE_ADMIN, ROLE_MEDECIN, ROLE_PATIENT"
    }
    USERS_ROLES {
        bigint user_id FK
        bigint role_id FK
    }
    SPECIALITES {
        bigint id PK
        varchar nom UK "Cardiologie, Generaliste..."
        varchar description
    }
    MEDECINS {
        bigint id PK
        bigint user_id FK,UK "1-1 vers USERS"
        varchar nom
        varchar prenom
        bigint specialite_id FK
        int duree_consultation_minutes "granularite des creneaux (def. 30)"
    }
    HORAIRES_TRAVAIL {
        bigint id PK
        bigint medecin_id FK
        tinyint jour_semaine "1=Lun .. 7=Dim"
        time heure_debut
        time heure_fin
    }
    PATIENTS {
        bigint id PK
        bigint user_id FK,UK "1-1 vers USERS"
        varchar nom
        varchar prenom
        varchar telephone
        date date_naissance
        varchar numero_dossier UK "unique"
    }
    RENDEZ_VOUS {
        bigint id PK
        bigint medecin_id FK
        bigint patient_id FK
        datetime date_heure_debut
        int duree_minutes
        varchar statut "PLANIFIE, CONFIRME, ANNULE, TERMINE"
        varchar motif
        datetime date_creation
    }
    DOSSIERS_MEDICAUX {
        bigint id PK
        bigint patient_id FK,UK "1-1 vers PATIENTS"
        datetime date_creation
    }
    CONSULTATIONS {
        bigint id PK
        bigint dossier_id FK
        bigint rendez_vous_id FK,UK "1-1 vers RENDEZ_VOUS"
        datetime date_consultation
        text notes "notes du medecin"
        text diagnostic
    }
    ORDONNANCES {
        bigint id PK
        bigint rendez_vous_id FK "medecin traitant = rdv.medecin"
        date date_emission
        int duree_validite_jours
    }
    LIGNES_ORDONNANCE {
        bigint id PK
        bigint ordonnance_id FK
        varchar medicament
        varchar posologie "ex: 1 cp 2x/jour"
        varchar duree_traitement
    }
    NOTIFICATIONS {
        bigint id PK
        bigint rendez_vous_id FK
        varchar type "EMAIL, SMS"
        varchar statut "ENVOYE, ECHEC"
        text contenu
        datetime date_envoi
    }
```

### Cardinalités

| Relation | Cardinalité | Sens |
|---|---|---|
| `users` ↔ `roles` | **N..N** (via `users_roles`) | Un utilisateur a un ou plusieurs rôles |
| `users` ↔ `medecins` | **1..1** | Un compte = un profil médecin (optionnel) |
| `users` ↔ `patients` | **1..1** | Un compte = un profil patient (optionnel) |
| `specialites` → `medecins` | **1..N** | Une spécialité regroupe plusieurs médecins |
| `medecins` → `horaires_travail` | **1..N** | Un médecin a plusieurs plages hebdo |
| `medecins` → `rendez_vous` | **1..N** | Un médecin a plusieurs rendez-vous |
| `patients` → `rendez_vous` | **1..N** | Un patient a plusieurs rendez-vous |
| `patients` → `dossiers_medicaux` | **1..1** | Un patient a un dossier unique |
| `dossiers_medicaux` → `consultations` | **1..N** | Un dossier regroupe les consultations |
| `rendez_vous` ↔ `consultations` | **1..1** | Un RDV terminé donne une consultation |
| `rendez_vous` → `ordonnances` | **1..N** | Un RDV peut générer des ordonnances |
| `ordonnances` → `lignes_ordonnance` | **1..N** | Une ordonnance liste plusieurs médicaments |
| `rendez_vous` → `notifications` | **1..N** | Un RDV déclenche des rappels |

---

## 2. Architecture en couches

```
com.clinique.api
├── ClinicApiApplication.java
├── config/          SecurityConfig · OpenApiConfig · DataInitializer (seed rôles/admin)
├── security/        JwtService · JwtAuthFilter · CustomUserDetailsService · UserDetailsImpl
├── model/           Entités JPA + enums (StatutRendezVous, TypeNotification, StatutNotification)
├── repository/      Interfaces Spring Data JPA
├── dto/
│   ├── request/     *Request (entrées validées avec Jakarta Validation)
│   └── response/    *Response (sorties, jamais l'entité brute)
├── service/         Logique métier
├── controller/      Endpoints REST (@RestController)
├── scheduler/       RappelScheduler (@Scheduled — rappels 24 h avant)
└── exception/       GlobalExceptionHandler + exceptions métier
```

---

## 3. DTOs prévus

| DTO | Champs principaux |
|---|---|
| `RegisterRequest` | nom, prenom, email, motDePasse, telephone, dateNaissance |
| `LoginRequest` | email, motDePasse |
| `MedecinRequest` | nom, prenom, email, motDePasse, specialiteId, dureeConsultationMinutes, horaires[] |
| `HoraireTravailRequest` | jourSemaine, heureDebut, heureFin |
| `SpecialiteRequest` | nom, description |
| `RendezVousRequest` | medecinId, dateHeureDebut |
| `ChangerStatutRequest` | statut |
| `ConsultationRequest` | rendezVousId, notes, diagnostic |
| `OrdonnanceRequest` | rendezVousId, dureeValiditeJours, lignes[] |
| `LigneOrdonnanceRequest` | medicament, posologie, dureeTraitement |

**Réponses (`dto/response`)**

| DTO | Rôle |
|---|---|
| `AuthResponse` | token, type=`Bearer`, expiresIn, roles, userId |
| `MedecinResponse` / `PatientResponse` | profils exposés (sans mot de passe) |
| `RendezVousResponse` | détail RDV + médecin/patient résumés |
| `CreneauDisponibleResponse` | heureDebut, heureFin (sortie du moteur de dispo) |
| `OrdonnanceResponse` / `DossierResponse` | données médicales filtrées par rôle |
| `ApiError` | timestamp, status, message, path, erreurs[] (format d'erreur uniforme) |

---

## 4. Matrice RBAC

| Domaine | Endpoint (exemple) | ADMIN | MEDECIN | PATIENT |
|---|---|:---:|:---:|:---:|
| Auth | `POST /api/auth/register`, `/login` | public | public | public |
| Spécialités | `CRUD /api/specialites` | ✅ | lecture | lecture |
| Médecins | `CRUD /api/medecins` | ✅ | son profil | lecture |
| Patients | `CRUD /api/patients` | ✅ | lecture | son profil |
| Disponibilités | `GET /api/medecins/{id}/disponibilites?date=` | ✅ | ✅ | ✅ |
| Horaires médecin | `PUT /api/medecins/{id}/horaires` | ✅ | les siens | ❌ |
| Rendez-vous | `POST /api/rendez-vous` (prise) | ✅ | ❌ | ✅ (le sien) |
| Rendez-vous | `PATCH /api/rendez-vous/{id}/statut` | ✅ | les siens | annuler le sien |
| Dossier médical | `GET /api/dossiers/{patientId}` | ✅ | ✅ (tous) | ✅ (le sien) |
| Ordonnances | `POST /api/ordonnances` | ✅ | médecin traitant seulement | ❌ |
| Ordonnances | `GET /api/ordonnances/mes` | ✅ | les siennes | les siennes |
