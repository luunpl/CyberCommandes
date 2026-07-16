package fr.uga.miage.l3.services;

import fr.uga.miage.l3.component.TourneeComponent;
import fr.uga.miage.l3.domain.models.Tournee;
import fr.uga.miage.l3.request.TourneeCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TourneeService {

    private final TourneeComponent tourneeComponent;

    //  On renvoie un objet Tournee pur
    public Tournee createTournee(TourneeCreationRequest request){
        // Ajoute ici tes règles métiers si besoin (ex: vérifier que l'heure de fin > heure de début)
        return tourneeComponent.createTournee(request);
    }

    //  On renvoie un Set de Tournee pur
    public Set<Tournee> getAllTournee(Long idJournee){
        return tourneeComponent.getAllTournee(idJournee);
    }

    public void deleteTournee(Long id) {
        tourneeComponent.deleteTournee(id);
    }
}