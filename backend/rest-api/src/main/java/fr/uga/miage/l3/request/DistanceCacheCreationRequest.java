package fr.uga.miage.l3.request;

import lombok.Builder;

@Builder
public record DistanceCacheCreationRequest(
        Double latDepart,
        Double lngDepart,
        Double latArrivee,
        Double lngArrivee,
        Double distance,
        Double temps
) {
}