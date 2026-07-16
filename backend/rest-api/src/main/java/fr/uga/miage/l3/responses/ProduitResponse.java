package fr.uga.miage.l3.responses;

import lombok.Data;

@Data
public class ProduitResponse {
    private Long id;
    private String nom;
    private Integer quantite;
    private Double prixUnitaire;
}