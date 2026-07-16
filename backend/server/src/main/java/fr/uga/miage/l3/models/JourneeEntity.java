package fr.uga.miage.l3.models;


import fr.uga.miage.l3.enums.StatutJournee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private StatutJournee statut;
    private LocalDate date;

    @OneToMany(mappedBy = "journeeEntity", cascade = CascadeType.ALL)
    private Set<TourneeEntity> tourneeEntities;


}