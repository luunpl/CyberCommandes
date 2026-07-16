package fr.uga.miage.l3.services;


import fr.uga.miage.l3.component.ClientComponent;
import fr.uga.miage.l3.domain.models.Client;
import fr.uga.miage.l3.exceptions.rest.BadRequestRestException;
import fr.uga.miage.l3.request.ClientCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientComponent clientComponent;

    public Client createClient(ClientCreationRequest request) {
        if (request.nom() == null || request.nom().isBlank()) {
            throw new BadRequestRestException("Creation with null or empty nom is not possible");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new BadRequestRestException("Creation with null or empty email is not possible");
        }

        return clientComponent.createClient(request);
    }
}
