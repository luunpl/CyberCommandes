package fr.uga.miage.l3.endpoints;

import fr.uga.miage.l3.request.ProduitCreationRequest;
import fr.uga.miage.l3.responses.ProduitResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "Gestion de Produit", description = "Tous les endpoints de Produit")
@RequestMapping("/api/produits")
public interface ProduitEndpoints {

    // Récupérer un produit
    @Operation(description = "Récupérer un produit par son ID")
    @ApiResponse(responseCode = "200", description = "Le produit a été récupéré")
    @ApiResponse(responseCode = "404", description = "Le produit demandé n'existe pas")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    ProduitResponse getProduit(@PathVariable(name = "id") Long id);

    // Récupération de tous les produits d'une commande
    @Operation(description = "Liste des Produits pour une Commande donnée")
    @ApiResponse(responseCode = "200", description = "La liste des produits de la commande")
    @ApiResponse(responseCode = "404", description = "La commande n'existe pas")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/commande/{idCommande}") // <-- On change l'URL
    Set<ProduitResponse> getAllProduitCommande(@PathVariable(name = "idCommande") Long idCommande);
    // Création d'un produit
    @Operation(description = "Création d'un produit dans le catalogue")
    @ApiResponse(responseCode = "201", description = "Le produit a bien été créé")
    @ApiResponse(responseCode = "400", description = "Erreur lors de la création du produit (données invalides)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    ProduitResponse createProduit(@RequestBody ProduitCreationRequest request);
}