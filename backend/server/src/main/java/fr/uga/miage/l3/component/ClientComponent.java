package fr.uga.miage.l3.component;


import fr.uga.miage.l3.domain.models.Client;
import fr.uga.miage.l3.mappers.ClientMapper;
import fr.uga.miage.l3.models.ClientEntity;
import fr.uga.miage.l3.repository.ClientRepository;
import fr.uga.miage.l3.request.ClientCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientComponent {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public Client createClient(ClientCreationRequest request) {
        ClientEntity entityToSave = clientMapper.toEntity(request);
        return clientMapper.toClient(clientRepository.save(entityToSave));
    }
}
