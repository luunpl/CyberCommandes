package fr.uga.miage.l3.models;

import fr.uga.miage.l3.enums.*;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivraisonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer nombreCommande;
    private StatutLivraison statut;

    @ManyToOne
    private TourneeEntity tourneeEntity;

    @OneToMany (mappedBy = "livraisonEntity")
    private Set<CommandeEntity> commandeEntities;

}