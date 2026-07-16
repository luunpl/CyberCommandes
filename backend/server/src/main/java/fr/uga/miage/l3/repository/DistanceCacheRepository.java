package fr.uga.miage.l3.repository;


import fr.uga.miage.l3.models.DistanceCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistanceCacheRepository extends JpaRepository<DistanceCacheEntity, Long> {
    Optional<DistanceCacheEntity> findByLatDepartAndLngDepartAndLatArriveeAndLngArrivee(Double latDepart, Double lngDepart, Double latArrivee, Double lngArrivee);
}
