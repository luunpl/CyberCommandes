package fr.uga.miage.l3.domain.models;


import fr.uga.miage.l3.enums.EtatCommande;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class Commande {
    private Long id;
    private String reference;
    private LocalDateTime dateCommande;
    private LocalDate dateLivraisonSouhaitee;
    private Double montant;
    private EtatCommande etatCommande;
    private Client client;
    private Long livraisonId; // On garde juste l'ID pour ne pas alourdir
    private Set<Produit> produits;
}
