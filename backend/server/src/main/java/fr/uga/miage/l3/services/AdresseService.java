package fr.uga.miage.l3.services;


import fr.uga.miage.l3.component.AdresseComponent;
import fr.uga.miage.l3.domain.models.Adresse;
import fr.uga.miage.l3.exceptions.rest.BadRequestRestException;
import fr.uga.miage.l3.request.AdresseCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdresseService {
    private final AdresseComponent adresseComponent;

    public Adresse createAdresse(AdresseCreationRequest request) {
        if (request.rue() == null || request.rue().isBlank()) {
            throw new BadRequestRestException("Creation with null or empty rue is not possible");
        }
        if (request.ville() == null || request.ville().isBlank()) {
            throw new BadRequestRestException("Creation with null or empty ville is not possible");
        }

        return adresseComponent.createAdresse(request);
    }
}
