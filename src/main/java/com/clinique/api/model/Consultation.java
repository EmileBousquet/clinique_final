package com.clinique.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierMedical dossier;

    @OneToOne
    @JoinColumn(name = "rendez_vous_id", unique = true, nullable = false)
    private RendezVous rendezVous;

    @Column(name = "date_consultation")
    private LocalDateTime dateConsultation;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String diagnostic;

    @PrePersist
    public void prePersist() {
        if (dateConsultation == null) {
            dateConsultation = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DossierMedical getDossier() {
        return dossier;
    }

    public void setDossier(DossierMedical dossier) {
        this.dossier = dossier;
    }

    public RendezVous getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
    }

    public LocalDateTime getDateConsultation() {
        return dateConsultation;
    }

    public void setDateConsultation(LocalDateTime dateConsultation) {
        this.dateConsultation = dateConsultation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }
}
