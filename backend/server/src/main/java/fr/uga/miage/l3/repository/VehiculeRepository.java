package fr.uga.miage.l3.repository;

import fr.uga.miage.l3.models.VehiculeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VehiculeRepository extends JpaRepository<VehiculeEntity,Long> {
}