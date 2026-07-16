package fr.uga.miage.l3.responses;

import fr.uga.miage.l3.enums.StatutJournee;
import lombok.Data;

import java.sql.Time;
import java.util.Set;

@Data

public class TourneeResponse {
    private Long id;
    private String algorithme;
    private Double distanceKm;
    private Double tempsHeure;
    private StatutJournee statut;
    private Set<Long> livraisonIds;
    private Long journeeId;
}
