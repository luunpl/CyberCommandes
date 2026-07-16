package fr.uga.miage.l3.domain.models;

import lombok.Data;

@Data
public class Produit {
    private Long id;
    private String nom;
    private Integer quantite;
    private Double prixUnitaire;
}