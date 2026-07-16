package fr.uga.miage.l3.mappers;
import fr.uga.miage.l3.domain.models.Commande;
import fr.uga.miage.l3.models.CommandeEntity;
import fr.uga.miage.l3.request.CommandeCreationRequest;
import fr.uga.miage.l3.responses.CommandeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {ClientMapper.class, ProduitMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommandeMapper {

    @Mapping(target = "client", source = "clientEntity")
    @Mapping(target = "produits", source = "produitEntities") // NOUVEAU
    @Mapping(target = "livraisonId", source = "livraisonEntity.id")
    Commande toCommande(CommandeEntity commandeEntity);
    Set<Commande> toCommandes(List<CommandeEntity> commandeEntity);

    CommandeResponse toResponse(Commande commande);

    Set<CommandeResponse> toResponses(Set<Commande> commandes);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientEntity", ignore = true)
    @Mapping(target = "produitEntities", ignore = true)
    @Mapping(target = "livraisonEntity", ignore = true)
    @Mapping(target = "etatCommande", constant = "OUVERTE")
    CommandeEntity toEntity(CommandeCreationRequest request);

}
