package fr.uga.miage.l3.responses;


import lombok.Data;

@Data
public class ClientResponse {
    private Long id;
    private String nom;
    private String email;
    private AdresseResponse adresse;
}
