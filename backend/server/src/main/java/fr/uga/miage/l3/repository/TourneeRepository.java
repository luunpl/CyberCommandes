package fr.uga.miage.l3.repository;

import fr.uga.miage.l3.models.TourneeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public interface TourneeRepository extends JpaRepository<TourneeEntity,Long> {

    Set<TourneeEntity> findByJourneeEntityId(Long id);

    //Set<TourneeEntity> findByVehiculeEntitiesImmatriculation(Long immatriculation);

    //Set<TourneeEntity> findByEquipeEntitiesId(Long id);

    //Set<TourneeEntity> findByEntrepotEntitiesId(Long id);


}