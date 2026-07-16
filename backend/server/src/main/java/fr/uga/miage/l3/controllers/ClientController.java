package fr.uga.miage.l3.controllers;


import fr.uga.miage.l3.endpoints.ClientEndpoints;
import fr.uga.miage.l3.mappers.ClientMapper;
import fr.uga.miage.l3.request.ClientCreationRequest;
import fr.uga.miage.l3.responses.ClientResponse;
import fr.uga.miage.l3.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientEndpoints {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @Override
    public ClientResponse createClient(ClientCreationRequest request) {
        return clientMapper.toResponse(clientService.createClient(request));
    }
}
