package fr.uga.miage.l3.it;

import fr.uga.miage.l3.request.AdresseCreationRequest;
import fr.uga.miage.l3.responses.AdresseResponse;
import fr.uga.miage.l3.repository.AdresseRepository; // Adapte selon ton package
import fr.uga.miage.l3.component.AdresseComponent;   // Adapte selon ton package
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase // Base H2 en mémoire
@AutoConfigureWebTestClient // Pour simuler les appels HTTP
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.sql.init.mode=never")
class AdresseControllerTestIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AdresseRepository adresseRepository;

    @MockitoSpyBean
    private AdresseComponent adresseComponent;

    @AfterEach
    public void clear() {
        adresseRepository.deleteAll();
    }

    // =========================================================================
    // TEST 1 : Création réussie (201 Created)
    // =========================================================================
    @Test
    void canCreateAdresse() {
        // Given : Utilisation du Builder comme demandé
        final AdresseCreationRequest request = AdresseCreationRequest
                .builder()
                .rue("1 Cours Jean Jaurès")
                .ville("Grenoble")
                .latitude(45.189)
                .longitude(5.715)
                .build();

        // When
        webTestClient
                .post()
                .uri("/api/adresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                // Then
                .expectStatus()
                .isCreated()
                .expectBody(AdresseResponse.class)
                .value(adresseResponse -> {
                    assertThat(adresseResponse.getId()).isNotNull();
                    assertThat(adresseResponse.getRue()).isEqualTo("1 Cours Jean Jaurès");

                    // Vérification que la donnée est bien dans la base H2
                    assertThat(adresseRepository.count()).isEqualTo(1);
                });
    }

    // =========================================================================
    // TEST 2 : Mauvaise requête (400 Bad Request)
    // =========================================================================
    @Test
    void cannotCreateAdresseWithBadRequest() {
        // Given : On crée une requête vide ou invalide pour forcer l'erreur


        // When
        webTestClient
                .post()
                .uri("/api/adresses")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus()
                .isBadRequest();
    }
}