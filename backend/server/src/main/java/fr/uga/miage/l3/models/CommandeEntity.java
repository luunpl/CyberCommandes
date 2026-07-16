package fr.uga.miage.l3.models;


import fr.uga.miage.l3.enums.EtatCommande;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;


@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reference;
    private LocalDateTime dateCommande;
    private LocalDate dateLivraisonSouhaitee;
    private Double montant;
    @Enumerated(EnumType.STRING)
    private EtatCommande etatCommande;

    @ManyToOne // Si on crée une commande, on crée/sauve le client
    @JoinColumn(name = "client_id")
    private ClientEntity clientEntity;

    @ManyToOne
    private LivraisonEntity livraisonEntity;

    @ManyToMany
    private Set<ProduitEntity> produitEntities;




}
