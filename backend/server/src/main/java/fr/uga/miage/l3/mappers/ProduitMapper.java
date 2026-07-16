package fr.uga.miage.l3.mappers;

import fr.uga.miage.l3.domain.models.Produit;
import fr.uga.miage.l3.models.ProduitEntity;
import fr.uga.miage.l3.request.ProduitCreationRequest;
import fr.uga.miage.l3.responses.ProduitResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProduitMapper {

    // Entity <-> Domaine
    Produit toProduit(ProduitEntity entity);
    Set<Produit> toProduits(Set<ProduitEntity> entities);

    // Domaine <-> Response
    ProduitResponse toResponse(Produit produit);
    Set<ProduitResponse> toResponses(Set<Produit> produits);

    // Request -> Entity (Pour la création)
    @Mapping(target = "id", ignore = true) // L'ID est généré par la BDD
    ProduitEntity toEntity(ProduitCreationRequest request);
}