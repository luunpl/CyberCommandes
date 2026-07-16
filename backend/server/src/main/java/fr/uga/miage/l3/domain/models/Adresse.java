package fr.uga.miage.l3.domain.models;


import lombok.Data;

@Data
public class Adresse {
    private Long id;
    private String rue;
    private String ville;
    private Double latitude;
    private Double longitude;
}
