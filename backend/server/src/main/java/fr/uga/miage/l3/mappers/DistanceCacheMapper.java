package fr.uga.miage.l3.mappers;


import fr.uga.miage.l3.models.DistanceCacheEntity;
import fr.uga.miage.l3.request.DistanceCacheCreationRequest;
import fr.uga.miage.l3.responses.DistanceCacheResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DistanceCacheMapper {

    DistanceCacheEntity toEntity(DistanceCacheCreationRequest request);

    List<DistanceCacheEntity> toEntityList(List<DistanceCacheCreationRequest> requests);

    DistanceCacheResponse toResponse(DistanceCacheEntity entity);
    List<DistanceCacheResponse> toResponseList(List<DistanceCacheEntity> entities);
}
