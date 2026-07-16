package fr.uga.miage.l3.endpoints;

import fr.uga.miage.l3.request.TourneeCreationRequest;
import fr.uga.miage.l3.responses.TourneeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name="tournee", description="gestion de tournee")
@RequestMapping("/api/tournee")
public interface TourneeEndpoints {

    @Operation(description = "Créer une nouvelle tournée après optimisation")
    @ApiResponse(responseCode = "201", description = "La tournée a bien été sauvegardée en base")
    @ApiResponse(responseCode = "400", description = "mauvaise requete")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TourneeResponse createTournee(@RequestBody TourneeCreationRequest request);

    @Operation(description = "recuperer toute les tournées d'une journee")
    @ApiResponse(responseCode = "200", description="les tournees sont recuperé")
    @ApiResponse(responseCode = "404", description="la journee nest pas trouvée")
    @GetMapping("/journee/{idJournee}")
    @ResponseStatus(HttpStatus.OK)
    Set<TourneeResponse> getAllTournee(@PathVariable Long idJournee);

    @Operation(description = "Supprimer une tournée existante")
    @ApiResponse(responseCode = "204", description = "La tournée a bien été supprimée")
    @ApiResponse(responseCode = "404", description = "La tournée n'a pas été trouvée")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTournee(@PathVariable Long id);
}