package com.clinique.api.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String ressource, Object identifiant) {
        super(ressource + " introuvable avec l'identifiant: " + identifiant);
    }
}
