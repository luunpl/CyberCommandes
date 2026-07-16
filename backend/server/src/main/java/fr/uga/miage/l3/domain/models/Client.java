package fr.uga.miage.l3.domain.models;


import lombok.Data;

@Data
public class Client {
    private Long id;
    private String nom;
    private String email;
    private Adresse adresse;
}
