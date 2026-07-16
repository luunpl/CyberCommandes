package fr.uga.miage.l3.controllers;

import fr.uga.miage.l3.endpoints.AdresseEndpoints;
import fr.uga.miage.l3.mappers.AdresseMapper;
import fr.uga.miage.l3.request.AdresseCreationRequest;
import fr.uga.miage.l3.responses.AdresseResponse;
import fr.uga.miage.l3.services.AdresseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class AdresseController implements AdresseEndpoints {

    private final AdresseService adresseService;
    private final AdresseMapper adresseMapper;

    @Override
    public AdresseResponse createAdresse(AdresseCreationRequest request){
        return adresseMapper.toResponse(adresseService.createAdresse(request));
    }

}
