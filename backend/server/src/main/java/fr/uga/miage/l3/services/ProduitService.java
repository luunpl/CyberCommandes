package fr.uga.miage.l3.services;

import fr.uga.miage.l3.component.ProduitComponent;
import fr.uga.miage.l3.domain.models.Produit;
import fr.uga.miage.l3.exceptions.rest.BadRequestRestException;
import fr.uga.miage.l3.exceptions.rest.NotFoundElementRestException; // Ou ProduitNotFoundRestException selon tes classes
import fr.uga.miage.l3.request.ProduitCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitComponent produitComponent;

    public Produit getProduitById(Long id) {
        try {
            return produitComponent.getProduit(id);
        } catch (Exception e) {
            throw new NotFoundElementRestException("Le produit avec l'ID [" + id + "] est introuvable.");
        }
    }

    public Set<Produit> getProduitsParCommande(Long idCommande) {
        try {
            return produitComponent.getProduitsParCommande(idCommande);
        } catch (Exception e) {
            throw new NotFoundElementRestException("La commande avec l'ID [" + idCommande + "] est introuvable.");
        }
    }

    public Produit createProduit(ProduitCreationRequest request) {
        if (request.nom() == null || request.nom().isBlank()) {
            throw new BadRequestRestException("Le nom du produit ne peut pas être vide.");
        }
        if (request.prixUnitaire() == null || request.prixUnitaire() < 0) {
            throw new BadRequestRestException("Le prix unitaire doit être positif.");
        }
        if (request.quantite() == null || request.quantite() < 0) {
            throw new BadRequestRestException("La quantité ne peut pas être négative.");
        }

        return produitComponent.createProduit(request);
    }
}