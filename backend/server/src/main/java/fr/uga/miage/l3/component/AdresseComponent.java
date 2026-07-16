package fr.uga.miage.l3.component;

import fr.uga.miage.l3.domain.models.Adresse;
import fr.uga.miage.l3.mappers.AdresseMapper;
import fr.uga.miage.l3.models.AdresseEntity;
import fr.uga.miage.l3.repository.AdresseRepository;
import fr.uga.miage.l3.request.AdresseCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AdresseComponent {

    private final AdresseRepository adresseRepository;
    private final AdresseMapper adresseMapper;

    public Adresse createAdresse(AdresseCreationRequest request) {
        AdresseEntity entityToSave = adresseMapper.toEntity(request);
        return adresseMapper.toAdresse(adresseRepository.save(entityToSave));
    }
}
