package fr.uga.miage.l3.controllers;


import fr.uga.miage.l3.endpoints.DistanceCacheEndpoints;
import fr.uga.miage.l3.mappers.DistanceCacheMapper;
import fr.uga.miage.l3.request.CoordinateRequest;
import fr.uga.miage.l3.request.DistanceCacheCreationRequest;
import fr.uga.miage.l3.responses.DistanceCacheResponse;
import fr.uga.miage.l3.responses.MatrixResponse;
import fr.uga.miage.l3.services.DistanceCacheService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DistanceCacheController implements DistanceCacheEndpoints {

    private final DistanceCacheService distanceCacheService;
    private final DistanceCacheMapper distanceCacheMapper;

    @Override
    public void saveMatrix(List<DistanceCacheCreationRequest> requests) {
        distanceCacheService.saveAllTrajets(distanceCacheMapper.toEntityList(requests));
    }

    @Override
    public List<DistanceCacheResponse> getAllCachedDistances() {
        return distanceCacheMapper.toResponseList(distanceCacheService.getAllTrajets());
    }

    @Override
    public ResponseEntity<MatrixResponse> getMatrixFromCache(@RequestBody List<CoordinateRequest> points) {
        MatrixResponse matrix = distanceCacheService.buildMatrixIfComplete(points);
        if (matrix == null) {
            return ResponseEntity.notFound().build(); // Renvoie 404 si le cache est incomplet
        }
        return ResponseEntity.ok(matrix); // Renvoie 200 avec les données
    }
}
