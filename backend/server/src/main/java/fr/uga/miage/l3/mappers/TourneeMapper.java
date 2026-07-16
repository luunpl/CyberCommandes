package fr.uga.miage.l3.mappers;

import fr.uga.miage.l3.domain.models.Tournee;
import fr.uga.miage.l3.models.TourneeEntity;
import fr.uga.miage.l3.request.TourneeCreationRequest;
import fr.uga.miage.l3.responses.TourneeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TourneeMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "livraisonEntities", ignore = true)
    TourneeEntity toEntity(TourneeCreationRequest request);




    @Mapping(target = "journeeId", source = "journeeEntity.id")
    // On doit ignorer ces listes d'IDs car nous n'avons plus les méthodes pour les traduire
    @Mapping(target = "livraisonIds", ignore = true)
    @Mapping(target = "equipesIds", ignore = true)
    Tournee toTournee(TourneeEntity entity);

    Set<Tournee> toTournees(Set<TourneeEntity> entities);


    // =========================================================================
    // 3. DOMAINE -> RESPONSE (Sortie vers le Front-end)
    // =========================================================================
    TourneeResponse toResponse(Tournee tournee);

    Set<TourneeResponse> toResponses(Set<Tournee> tournees);
}