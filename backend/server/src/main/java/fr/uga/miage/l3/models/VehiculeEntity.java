package fr.uga.miage.l3.models;

import fr.uga.miage.l3.enums.StatutVehicule;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String immatriculation;
    private Double capacite;
    private StatutVehicule statut;
    private Double consommation;
    private String modele;

    @ManyToMany(mappedBy = "vehiculeEntities")
    private Set<TourneeEntity> tourneeEntities;
}