package fr.uga.miage.l3.repository;


import fr.uga.miage.l3.models.*;
import fr.uga.miage.l3.enums.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JourneeRepository extends JpaRepository<JourneeEntity, Long> {

    //List<JourneeEntity> findByStatut(StatutJournee statut);

    //Optional<JourneeEntity> findByDate(LocalDate date);

    //boolean existsByDate(LocalDate date);
}