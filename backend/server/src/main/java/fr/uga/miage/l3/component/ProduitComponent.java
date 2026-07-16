package fr.uga.miage.l3.component;

import fr.uga.miage.l3.domain.models.Produit;
import fr.uga.miage.l3.mappers.ProduitMapper;
import fr.uga.miage.l3.models.CommandeEntity;
import fr.uga.miage.l3.models.ProduitEntity;
import fr.uga.miage.l3.repository.CommandeRepository;
import fr.uga.miage.l3.repository.ProduitRepository;
import fr.uga.miage.l3.request.ProduitCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProduitComponent {

    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;
    private final ProduitMapper produitMapper;

    public Produit getProduit(Long id) {
        ProduitEntity entity = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé")); // Utilise ton Exception Technique ici
        return produitMapper.toProduit(entity);
    }

    public Produit createProduit(ProduitCreationRequest request) {
        ProduitEntity entityToSave = produitMapper.toEntity(request);
        ProduitEntity saved = produitRepository.save(entityToSave);
        return produitMapper.toProduit(saved);
    }

    public Set<Produit> getProduitsParCommande(Long idCommande) {
        // On cherche la commande par son ID classique
        CommandeEntity commande = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée")); // À remplacer par ton Exception technique

        // On récupère directement les produits liés à cette commande
        return produitMapper.toProduits(commande.getProduitEntities());
    }
}