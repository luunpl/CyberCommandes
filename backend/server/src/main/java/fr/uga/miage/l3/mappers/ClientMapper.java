package fr.uga.miage.l3.mappers;


import fr.uga.miage.l3.domain.models.Client;
import fr.uga.miage.l3.models.ClientEntity;
import fr.uga.miage.l3.request.ClientCreationRequest;
import fr.uga.miage.l3.responses.ClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {AdresseMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    @Mapping(target = "adresse", source = "adresseEntity")
    Client toClient(ClientEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "adresseEntity", source = "adresse")
    ClientEntity toEntity(ClientCreationRequest request);

    ClientResponse toResponse(Client client);
}
