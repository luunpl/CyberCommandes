package fr.uga.miage.l3.services;


import fr.uga.miage.l3.component.CommandeComponent;
import fr.uga.miage.l3.domain.models.Commande;
import fr.uga.miage.l3.exceptions.rest.BadRequestRestException;
import fr.uga.miage.l3.exceptions.rest.ClientEntityNotFoundRestException;
import fr.uga.miage.l3.exceptions.rest.CommandeNotFoundRestException;
import fr.uga.miage.l3.exceptions.technical.ClientEntityNotFoundException;
import fr.uga.miage.l3.exceptions.technical.CommandeEntityNotFoundException;
import fr.uga.miage.l3.request.CommandeCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeComponent commandeComponent;

    public Commande getCommandeById(Long idCommande) {
        try {
            return commandeComponent.getCommande(idCommande);
        } catch (CommandeEntityNotFoundException e) {
            throw new CommandeNotFoundRestException(e.getMessage());
        }
    }

    public Commande createCommande(CommandeCreationRequest request) {

        if (request.reference() == null || request.reference().isBlank()) {
            throw new BadRequestRestException("Creation with null or empty reference is not possible");

        }

        if (request.clientId() == null) {
            throw new BadRequestRestException("Création with null clientId is not possible");

        }

        if (request.montant() != null && request.montant() < 0) {
            throw new BadRequestRestException("Creation with negative montant is not possible");

        }
        try {
            return commandeComponent.createCommande(request);
        } catch (ClientEntityNotFoundException e) {
            throw new ClientEntityNotFoundRestException(e.getMessage());
        }
    }

    public Set<Commande> getCommandesByDate(LocalDate date) {
        return commandeComponent.getCommandesByDate(date);
    }
}
