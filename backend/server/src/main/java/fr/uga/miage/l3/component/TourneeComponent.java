package fr.uga.miage.l3.component;

import fr.uga.miage.l3.domain.models.Tournee; // CHANGÉ : On importe le modèle du domaine
import fr.uga.miage.l3.exceptions.rest.NotFoundElementRestException;
import fr.uga.miage.l3.mappers.TourneeMapper;
import fr.uga.miage.l3.models.JourneeEntity;
import fr.uga.miage.l3.models.TourneeEntity;
import fr.uga.miage.l3.repository.JourneeRepository;
import fr.uga.miage.l3.repository.TourneeRepository;
import fr.uga.miage.l3.request.TourneeCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class TourneeComponent {
    private final TourneeRepository tourneeRepository;
    private final TourneeMapper tourneeMapper;
    private final JourneeRepository journeeRepository;


    public Tournee createTournee(TourneeCreationRequest request) {
        TourneeEntity entityToSave = tourneeMapper.toEntity(request);

        if (request.journeeId() != null) {
            JourneeEntity journee = journeeRepository.findById(request.journeeId())
                    .orElseThrow(() -> new NotFoundElementRestException("La journee [" + request.journeeId() + "] n'existe pas"));
            entityToSave.setJourneeEntity(journee);
        }


        TourneeEntity savedEntity = tourneeRepository.save(entityToSave);
        return tourneeMapper.toTournee(savedEntity);
    }


    public Set<Tournee> getAllTournee(Long idJournee){
        // 1. On vérifie d'abord si la journée existe !
        if (!journeeRepository.existsById(idJournee)) {
            throw new NotFoundElementRestException("La journee n'existe pas");
        }

        // CHANGÉ : On récupère les entités, et on les mappe en objets purs
        Set<TourneeEntity> entities = tourneeRepository.findByJourneeEntityId(idJournee);
        return tourneeMapper.toTournees(entities);
    }

    public void deleteTournee(Long id) {
        // On vérifie que la tournée existe bien
        if (!tourneeRepository.existsById(id)) {
            throw new NotFoundElementRestException("La tournée [" + id + "] n'existe pas");
        }

        // On supprime
        tourneeRepository.deleteById(id);
    }
}