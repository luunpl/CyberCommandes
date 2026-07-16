package fr.uga.miage.l3.endpoints;


import fr.uga.miage.l3.request.CoordinateRequest;
import fr.uga.miage.l3.request.DistanceCacheCreationRequest;
import fr.uga.miage.l3.responses.DistanceCacheResponse;
import fr.uga.miage.l3.responses.MatrixResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cache distances", description = "Gestion du cache pouir l'optimisation ORS")
@RequestMapping("/api/distances")
public interface DistanceCacheEndpoints {

    @Operation(description = "Sauvegarder une matrice entière de trajets")
    @ApiResponse(responseCode = "201", description = "Les trajets ont été sauvegardés en cache")
    @PostMapping("/save-batch")
    @ResponseStatus(HttpStatus.CREATED)
    void saveMatrix(@RequestBody List<DistanceCacheCreationRequest> trajets);

    @Operation(description = "Recupère tous les trajets en cache")
    @ApiResponse(responseCode = "200", description = "Retourne la base de connaissances des trajets")
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    List<DistanceCacheResponse> getAllCachedDistances();


    @Operation(description = "Récupérer une matrice depuis le cache")
    @ApiResponse(responseCode = "200", description = "Matrice complète trouvée")
    @ApiResponse(responseCode = "404", description = "Cache incomplet")
    @PostMapping("/get-matrix")
    ResponseEntity<MatrixResponse> getMatrixFromCache(@RequestBody List<CoordinateRequest> points);

}
