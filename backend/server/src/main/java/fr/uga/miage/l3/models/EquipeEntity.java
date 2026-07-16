package fr.uga.miage.l3.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @ManyToMany(mappedBy = "equipeEntities")
    private Set<TourneeEntity> tourneeEntities;

    @ManyToMany
    private Set<LivreurEntity> livreurEntities;
}