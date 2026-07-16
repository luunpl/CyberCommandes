package fr.uga.miage.l3.request;

public record ProduitCreationRequest(
        String nom,
        Integer quantite,
        Double prixUnitaire
) {
}