package fr.uga.miage.l3.controllers;

import fr.uga.miage.l3.endpoints.ProduitEndpoints;
import fr.uga.miage.l3.mappers.ProduitMapper;
import fr.uga.miage.l3.request.ProduitCreationRequest;
import fr.uga.miage.l3.responses.ProduitResponse;
import fr.uga.miage.l3.services.ProduitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class ProduitController implements ProduitEndpoints {

    private final ProduitService produitService;
    private final ProduitMapper produitMapper;

    @Override
    public ProduitResponse getProduit(Long id) {
        return produitMapper.toResponse(produitService.getProduitById(id));
    }

    @Override
    public Set<ProduitResponse> getAllProduitCommande(Long idCommande) {
        return produitMapper.toResponses(produitService.getProduitsParCommande(idCommande));
    }

    @Override
    public ProduitResponse createProduit(ProduitCreationRequest request) {
        return produitMapper.toResponse(produitService.createProduit(request));
    }
}