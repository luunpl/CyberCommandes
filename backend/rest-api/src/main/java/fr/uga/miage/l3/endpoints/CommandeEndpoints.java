package fr.uga.miage.l3.endpoints;


import fr.uga.miage.l3.request.CommandeCreationRequest;
import fr.uga.miage.l3.responses.CommandeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@Tag(name = "Commande", description = "Gestion des commandes")
@RequestMapping("/api/commandes")
public interface CommandeEndpoints {

    @Operation(description = "Récupérer une commande par ID")
    @ApiResponse(responseCode = "200", description = "La commande a été trouvée")
    @ApiResponse(responseCode = "404", description = "La commande n'existe pas")
    @GetMapping("/{idCommande}")
    @ResponseStatus(HttpStatus.OK)
    CommandeResponse getCommandeById(@PathVariable Long idCommande);

    @Operation(description = "Créer une nouvelle commande pour un client existant")
    @ApiResponse(responseCode = "201", description = "La commande a bien été créée")
    @ApiResponse(responseCode = "400", description = "La requête est invalide (référence ou clientId manquant)")
    @ApiResponse(responseCode = "404", description = "Le client spécifié n'existe pas")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CommandeResponse createCommande(@RequestBody CommandeCreationRequest request);

    @Operation(description = "Récupérer toutes les commandes")
    @ApiResponse(responseCode = "200", description = "Liste des commandes récupérées")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    Set<CommandeResponse> getAllCommandes(@RequestParam(required = false) LocalDate dateLivraisonSouhaitee);





}
