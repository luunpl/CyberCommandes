package fr.uga.miage.l3.controllers;

import fr.uga.miage.l3.endpoints.TourneeEndpoints;
import fr.uga.miage.l3.mappers.TourneeMapper;

import fr.uga.miage.l3.request.TourneeCreationRequest;
import fr.uga.miage.l3.responses.TourneeResponse;
import fr.uga.miage.l3.services.TourneeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


@RestController
@RequiredArgsConstructor
public class TourneeController implements TourneeEndpoints {

    private final TourneeService tourneeService;
    private final TourneeMapper tourneeMapper;

    @Override
    public TourneeResponse createTournee(TourneeCreationRequest request){
        return tourneeMapper.toResponse(tourneeService.createTournee(request));
    }

    @Override
    public Set<TourneeResponse> getAllTournee(Long idJournee){
        return tourneeMapper.toResponses(tourneeService.getAllTournee(idJournee));
    }

    @Override
    public void deleteTournee(Long id) {
        tourneeService.deleteTournee(id);
    }

}