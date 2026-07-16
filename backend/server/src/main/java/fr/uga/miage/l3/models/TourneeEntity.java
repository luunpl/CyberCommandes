package fr.uga.miage.l3.models;

import fr.uga.miage.l3.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourneeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String algorithme;
    private Double distanceKm;
    private Double tempsHeure;
    @Enumerated(EnumType.STRING)
    private StatutJournee statut;

    @ManyToOne
    private JourneeEntity journeeEntity;

    @ManyToMany
    private Set<EntrepotEntity> entrepotEntities;

    @OneToMany(mappedBy = "tourneeEntity")
    private Set<LivraisonEntity> livraisonEntities;

    @ManyToMany
    private Set<EquipeEntity> equipeEntities;

    @ManyToMany
    private Set<VehiculeEntity> vehiculeEntities;


}