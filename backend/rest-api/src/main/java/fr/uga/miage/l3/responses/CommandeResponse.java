package fr.uga.miage.l3.responses;

import fr.uga.miage.l3.enums.EtatCommande;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class CommandeResponse {
    private Long id;
    private String reference;

    private LocalDateTime dateCommande;

    private Double montant;

    private EtatCommande etatCommande;

    private ClientResponse client;
    private Long livraisonId; //  Permet au front de savoir si c'est planifié
    private Set<ProduitResponse> produits;
}
