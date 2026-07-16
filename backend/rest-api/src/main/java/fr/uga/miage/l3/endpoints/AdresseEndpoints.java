package fr.uga.miage.l3.endpoints;


import fr.uga.miage.l3.request.AdresseCreationRequest;
import fr.uga.miage.l3.responses.AdresseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Adresse", description = "Gestion des adresses")
@RequestMapping("/api/adresses")
public interface AdresseEndpoints {
    @Operation(description = "Créer une nouvelle adresse")
    @ApiResponse(responseCode = "201", description = "L'adresse a bien été créée")
    @ApiResponse(responseCode = "400", description = "La requête est invalide")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdresseResponse createAdresse(@RequestBody AdresseCreationRequest request);

}
