package fr.uga.miage.l3.endpoints;

import fr.uga.miage.l3.request.ClientCreationRequest;
import fr.uga.miage.l3.responses.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name ="Client", description = "Gestion des clients")
@RequestMapping("/api/clients")

public interface ClientEndpoints {
    @Operation(description = "Créer un nouveau client avec son adresse")
    @ApiResponse(responseCode = "201", description = "Le client a bien été crée")
    @ApiResponse(responseCode = "400", description = "La requête est invalide")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ClientResponse createClient(@RequestBody ClientCreationRequest request);
}
