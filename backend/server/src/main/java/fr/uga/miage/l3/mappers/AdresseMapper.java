package fr.uga.miage.l3.mappers;


import fr.uga.miage.l3.domain.models.Adresse;
import fr.uga.miage.l3.models.AdresseEntity;
import fr.uga.miage.l3.request.AdresseCreationRequest;
import fr.uga.miage.l3.responses.AdresseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdresseMapper {
    Adresse toAdresse(AdresseEntity entity);

    @Mapping(target = "id", ignore = true)
    AdresseEntity toEntity(AdresseCreationRequest request);
    AdresseResponse toResponse(Adresse adresse);
}
