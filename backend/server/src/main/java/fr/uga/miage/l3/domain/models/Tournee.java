package fr.uga.miage.l3.domain.models;

import fr.uga.miage.l3.enums.StatutJournee;
import lombok.Data;

import java.sql.Time;
import java.util.Set;

@Data
public class Tournee {

    // Informations basiques
    private Long id;
    private String algorithme;
    private Double distanceKm;
    private Double tempsHeure;
    private StatutJournee statut;

    // Relations (stockées sous forme d'IDs pour simplifier le transport et le mapping)
    private Long journeeId;
    private Set<Long> entrepotIds;
    private Set<Long> livraisonIds;
    private Set<Long> equipesIds; // Attention au "s" à equipesIds pour matcher ton DTO !
    private Set<Long> vehiculeIds;
}