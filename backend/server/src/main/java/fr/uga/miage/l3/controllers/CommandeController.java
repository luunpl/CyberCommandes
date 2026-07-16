package fr.uga.miage.l3.controllers;


import fr.uga.miage.l3.endpoints.CommandeEndpoints;
import fr.uga.miage.l3.mappers.CommandeMapper;
import fr.uga.miage.l3.request.CommandeCreationRequest;
import fr.uga.miage.l3.responses.CommandeResponse;
import fr.uga.miage.l3.services.CommandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class CommandeController implements CommandeEndpoints {

    private final CommandeService commandeService;
    private final CommandeMapper commandeMapper;

    @Override
    public CommandeResponse getCommandeById(Long idCommande) {
        return  commandeMapper.toResponse(commandeService.getCommandeById(idCommande));
    }

    @Override
    public CommandeResponse createCommande(CommandeCreationRequest request){
        return commandeMapper.toResponse(commandeService.createCommande(request));
    }

    @Override
    public Set<CommandeResponse> getAllCommandes(LocalDate dateLivraisonSouhaitee) {
        return commandeMapper.toResponses(commandeService.getCommandesByDate(dateLivraisonSouhaitee));
    }


}
