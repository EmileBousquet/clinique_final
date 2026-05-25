package com.clinique.api.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordonnances")
public class Ordonnance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rendez_vous_id", nullable = false)
    private RendezVous rendezVous;

    @Column(name = "date_emission", nullable = false)
    private LocalDate dateEmission;

    @Column(name = "duree_validite_jours")
    private Integer dureeValiditeJours;

    @OneToMany(mappedBy = "ordonnance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneOrdonnance> lignes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateEmission == null) {
            dateEmission = LocalDate.now();
        }
    }

    public void ajouterLigne(LigneOrdonnance ligne) {
        ligne.setOrdonnance(this);
        this.lignes.add(ligne);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RendezVous getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
    }

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDate dateEmission) {
        this.dateEmission = dateEmission;
    }

    public Integer getDureeValiditeJours() {
        return dureeValiditeJours;
    }

    public void setDureeValiditeJours(Integer dureeValiditeJours) {
        this.dureeValiditeJours = dureeValiditeJours;
    }

    public List<LigneOrdonnance> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneOrdonnance> lignes) {
        this.lignes = lignes;
    }
}
