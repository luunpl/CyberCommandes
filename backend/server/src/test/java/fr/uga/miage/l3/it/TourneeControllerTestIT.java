package fr.uga.miage.l3.it;

import fr.uga.miage.l3.component.TourneeComponent;
import fr.uga.miage.l3.errors.ErrorDTO;
import fr.uga.miage.l3.models.JourneeEntity;
import fr.uga.miage.l3.models.TourneeEntity;
import fr.uga.miage.l3.repository.JourneeRepository;
import fr.uga.miage.l3.repository.TourneeRepository;
import fr.uga.miage.l3.request.TourneeCreationRequest;
import fr.uga.miage.l3.responses.TourneeResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Set;

import static fr.uga.miage.l3.enums.StatutJournee.EN_COURS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@AutoConfigureWebTestClient
@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.sql.init.mode=never")
class TourneeControllerTestIT {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private TourneeRepository tourneeRepository;
    @Autowired
    private JourneeRepository journeeRepository;
    @MockitoSpyBean
    private TourneeComponent tourneeComponent;

    @AfterEach
    public void clear(){
        tourneeRepository.deleteAll();
        journeeRepository.deleteAll();
    }

    @Test
    void canCreateTourneeSuccessfully(){
        //Given On prépare une vraie journée en base, puis la requête
        JourneeEntity savedJournee = journeeRepository.save(JourneeEntity.builder().build());


        final TourneeCreationRequest request = new TourneeCreationRequest(
                "K_Means",             // algorithme
                120.5,                 // distanceKm
                2.5,                   // tempsHeure
                EN_COURS,              // statut
                Set.of(),              // livraisonIds
                savedJournee.getId()   // journeeId
        );

        //When
        webTestClient
                .post()
                .uri("/api/tournee")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                //Then
                .expectStatus()
                .isCreated()
                .expectBody(TourneeResponse.class)
                .value(responseDTO -> {
                    assertThat(tourneeRepository.count()).isEqualTo(1); //On verifie que la bd contient bien 1 tournee
                    assertThat(responseDTO.getAlgorithme()).isEqualTo("K_Means"); //on verifie que le retour contient bien l'algo K_Means
                });
    }

    @Test
    void cannotCreateTourneeWhenJourneeNotFound(){
        //Given On cree une requete avec un ID de journee inexistant

        final TourneeCreationRequest request = new TourneeCreationRequest(
                "Greedy",              // algorithme
                165.0,                 // distanceKm
                13.0,                  // tempsHeure
                EN_COURS,              // statut
                Set.of(),              // livraisonIds
                999L                   // journeeId
        );
        ErrorDTO expectedError = new ErrorDTO("/api/tournee","La journee [" + request.journeeId() + "] n'existe pas");

        //When
        webTestClient
                .post()
                .uri("/api/tournee")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                //Then
                .expectStatus().isNotFound()
                .expectBody(ErrorDTO.class)
                .value(error ->{
                    assertThat(tourneeRepository.count()).isEqualTo(0);
                });
    }

    /*
    ===================================================================
    Pour le GET
    ===================================================================
    */

    @Test
    void canGetAllTourneesByJourneeId() {
        // Given On crée une journée et deux tournées rattachées en bd
        JourneeEntity journee = journeeRepository.save(JourneeEntity.builder().build());

        TourneeEntity t1 = TourneeEntity.builder()
                .algorithme("ALGO_1")
                .journeeEntity(journee)
                .build();
        TourneeEntity t2 = TourneeEntity.builder()
                .algorithme("ALGO_2")
                .journeeEntity(journee)
                .build();

        tourneeRepository.saveAll(Set.of(t1, t2));

        // When On appelle le GET avec l'ID de la journée qu'on vient de créer
        webTestClient
                .get()
                .uri("/api/tournee/journee/{idJournee}", journee.getId())
                .exchange()
                // Then On vérifie le résultat
                .expectStatus().isOk()
                .expectBodyList(TourneeResponse.class)
                .value(resultList -> {
                    // On vérifie que la liste contient bien nos 2 tournées
                    assertThat(resultList.size()).isEqualTo(2);
                    // On vérifie qu'on retrouve bien la tournée avec l'algo 1
                    boolean contientAlgo1 = resultList.stream().anyMatch(t -> "ALGO_1".equals(t.getAlgorithme()));
                    assertThat(contientAlgo1).isTrue();
                });
    }

    @Test
    void cannotGetTourneesWhenJourneeNotFound() {
        // Given On prépare l'erreur exacte qu'on s'attend à recevoir (pour l'ID 999)
        ErrorDTO expectedError = new ErrorDTO("/api/tournee/journee/999", "La journee n'existe pas");

        // When On fait l'appel GET
        webTestClient
                .get()
                .uri("/api/tournee/journee/999")
                .exchange()
                // Then On vérifie que c'est bien une erreur 404
                .expectStatus().isNotFound()
                .expectBody(ErrorDTO.class)
                .value(error -> {
                    // On compare que l'erreur reçue est exactement notre ErrorDTO attendu
                    assertThat(error).usingRecursiveComparison().isEqualTo(expectedError);
                });
    }

    /*
    ===================================================================
    Pour le DELETE
    ===================================================================
    */

    @Test
    void canDeleteTourneeSuccessfully() {
        // Given On prépare une journée et une tournée en base de données
        JourneeEntity journee = journeeRepository.save(JourneeEntity.builder().build());
        TourneeEntity savedTournee = tourneeRepository.save(TourneeEntity.builder().journeeEntity(journee).build());
        Long idToDelete = savedTournee.getId();

        // When : On fait l'appel DELETE
        webTestClient
                .delete()
                .uri("/api/tournee/{id}", idToDelete)
                .exchange()
                .expectStatus().isNoContent();

        // Then On vérifie dans la vraie base de données que la tournée a bien disparu
        assertThat(tourneeRepository.existsById(idToDelete)).isFalse();
    }

    @Test
    void cannotDeleteTourneeWhenNotFound() {
        // Given : On choisit un ID qui n'existe pas
        Long idToDelete = 999L;
        // On prépare l'erreur exacte générée par ton PIExceptionHandler
        ErrorDTO expectedError = new ErrorDTO("/api/tournee/" + idToDelete, "La tournée [" + idToDelete + "] n'existe pas");

        // When : On fait l'appel DELETE
        webTestClient
                .delete()
                .uri("/api/tournee/{id}", idToDelete)
                .exchange()
                // Then : On vérifie que ton architecture a bien renvoyé une 404 avec l'ErrorDTO
                .expectStatus().isNotFound()
                .expectBody(ErrorDTO.class)
                .value(error -> {
                    assertThat(error).usingRecursiveComparison().isEqualTo(expectedError);
                });
    }
}