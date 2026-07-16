package fr.uga.miage.l3.responses;

import lombok.Data;

@Data
public class AdresseResponse {
    private Long id;
    private String rue;
    private String ville;
    private Double latitude;
    private Double longitude;
}
