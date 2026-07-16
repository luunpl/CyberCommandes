package fr.uga.miage.l3.request;


import lombok.Builder;

@Builder
public record AdresseCreationRequest(
        String rue,
        String ville,
        Double latitude,

        Double longitude
) {
}
