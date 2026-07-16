package fr.uga.miage.l3.component;


import fr.uga.miage.l3.domain.models.Commande;
import fr.uga.miage.l3.exceptions.technical.ClientEntityNotFoundException;
import fr.uga.miage.l3.exceptions.technical.CommandeEntityNotFoundException;
import fr.uga.miage.l3.mappers.CommandeMapper;
import fr.uga.miage.l3.models.ClientEntity;
import fr.uga.miage.l3.models.CommandeEntity;
import fr.uga.miage.l3.models.ProduitEntity;
import fr.uga.miage.l3.repository.ClientRepository;
import fr.uga.miage.l3.repository.CommandeRepository;
import fr.uga.miage.l3.repository.ProduitRepository;
import fr.uga.miage.l3.request.CommandeCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CommandeComponent {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final CommandeMapper commandeMapper;
    private final ProduitRepository produitRepository;

    public Commande getCommande(Long idCommande) throws CommandeEntityNotFoundException {
        return commandeMapper.toCommande(commandeRepository.findById(idCommande)
                .orElseThrow(() -> new CommandeEntityNotFoundException(String.format("La commande [%s] n'existe pas", idCommande))));
    }



    public Commande createCommande(CommandeCreationRequest request) throws ClientEntityNotFoundException {
        // 1. Transformation
        CommandeEntity entity = commandeMapper.toEntity(request);

        // 2. Récupération et liaison du Client
        ClientEntity client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ClientEntityNotFoundException(String.format("Le client [%s] n'existe pas", request.clientId())));
        entity.setClientEntity(client);

        // 3. Récupération et liaison des Produits (NOUVEAU)
        if (request.produitIds() != null && !request.produitIds().isEmpty()) {
            List<ProduitEntity> produits = produitRepository.findAllById(request.produitIds());
            entity.setProduitEntities(new HashSet<>(produits));
        }

        //  Sauvegarde
        CommandeEntity saved = commandeRepository.save(entity);
        return commandeMapper.toCommande(saved);
    }

    public Set<Commande> getCommandesByDate(LocalDate date) {
        if (date == null) {
            // Si pas de date, on renvoie tout
            return commandeMapper.toCommandes(commandeRepository.findAll());
        }
        // Sinon on filtre par la date
        return commandeMapper.toCommandes(commandeRepository.findByDateLivraisonSouhaitee(date));
    }



}
