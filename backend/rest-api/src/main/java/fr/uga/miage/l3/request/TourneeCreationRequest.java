package fr.uga.miage.l3.request;

import fr.uga.miage.l3.enums.StatutJournee;

import java.sql.Time;
import java.util.Set;

public record TourneeCreationRequest(
        String algorithme,

        Double distanceKm,
        Double tempsHeure,
        StatutJournee statut,
        Set<Long> livraisonIds,

        Long journeeId
){}