package fr.uga.miage.l3.request;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;


@Builder
public record CommandeCreationRequest(
        String reference,
        Double montant,

        LocalDateTime dateCommande,

        Long clientId,

        Set<Long> produitIds


) {
}
