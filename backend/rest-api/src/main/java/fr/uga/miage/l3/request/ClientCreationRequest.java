package fr.uga.miage.l3.request;

public record ClientCreationRequest(
        String nom,
        String email,
        AdresseCreationRequest adresse
) {
}
