package fr.uga.miage.l3.it;

import fr.uga.miage.l3.errors.ErrorDTO;
import fr.uga.miage.l3.repository.ClientRepository;
import fr.uga.miage.l3.request.AdresseCreationRequest;
import fr.uga.miage.l3.request.ClientCreationRequest;
import fr.uga.miage.l3.responses.ClientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@AutoConfigureWebTestClient
class ClientControllerTestIT {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void setup(){
        clientRepository.deleteAll();
    }

    @Test
    @DisplayName("POST-201 client created")
    void createClient() {
        // when
        webTestClient
                .post()
                .uri("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientCreationRequest(
                        "Alice",
                        "alice@test.fr",
                        new AdresseCreationRequest("1 rue de Grenoble", "Grenoble", 45.188, 5.724)
                ))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(ClientResponse.class)
                .value(s -> {
                    //then
                    assertThat(s.getNom()).isEqualTo("Alice");
                    assertThat(s.getEmail()).isEqualTo("alice@test.fr");
                    assertThat(clientRepository.count()).isOne();
                });
    }

    @Test
    @DisplayName("POST-400 client not created because nom is null")
    void createClientWithNullNom() {
        ErrorDTO expectedError = new ErrorDTO(
                "/api/clients",
                "Creation with null or empty nom is not possible"
        );

        // when
        webTestClient
                .post()
                .uri("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientCreationRequest(
                        null,
                        "alice@test.fr",
                        new AdresseCreationRequest("1 rue de Grenoble", "Grenoble", 45.188, 5.724)
                ))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ErrorDTO.class)
                .value(s -> {
                    assertThat(s).isEqualTo(expectedError);
                });
    }
}