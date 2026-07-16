package fr.uga.miage.l3.repository;


import fr.uga.miage.l3.models.EntrepotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntrepotRepository extends JpaRepository<EntrepotEntity, Long> {
}