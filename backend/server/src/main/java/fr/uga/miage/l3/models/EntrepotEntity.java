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
public class EntrepotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;

    @ManyToOne
    private AdresseEntity adresseEntity;

    @ManyToMany(mappedBy = "entrepotEntities")
    private Set<TourneeEntity> tourneeEntities;


}