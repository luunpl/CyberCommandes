package fr.uga.miage.l3.it;

import fr.uga.miage.l3.models.CommandeEntity;
import fr.uga.miage.l3.request.CommandeCreationRequest;
import fr.uga.miage.l3.responses.CommandeResponse;
import fr.uga.miage.l3.repository.CommandeRepository;
import fr.uga.miage.l3.repository.ClientRepository;
import fr.uga.miage.l3.models.ClientEntity; // Adapte l'import
import fr.uga.miage.l3.component.CommandeComponent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.sql.init.mode=never")
class CommandeControllerTestIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private ClientRepository clientRepository; // Nécessaire car une commande a besoin d'un client

    @MockitoSpyBean
    private CommandeComponent commandeComponent;

    @AfterEach
    public void clear() {

        commandeRepository.deleteAll();
        clientRepository.deleteAll();
    }

    // =========================================================================
    // TEST 1 : Création réussie (201 Created)
    // =========================================================================
    @Test
    void canCreateCommande() {
        // Given : On doit d'abord créer un client en base pour que la commande puisse s'y attacher
        ClientEntity fauxClient = new ClientEntity();
        // Remplis ici les champs obligatoires de ton ClientEntity (nom, email, etc.)
        fauxClient = clientRepository.save(fauxClient);

        final CommandeCreationRequest request = CommandeCreationRequest.builder()
                .reference("CMD-2026-001")
                .montant(150.50)
                .dateCommande(LocalDateTime.now())
                .clientId(fauxClient.getId()) // On utilise l'ID du client qu'on vient de créer
                .produitIds(Set.of())
                .build();

        // When
        webTestClient
                .post()
                .uri("/api/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                // Then
                .expectStatus()
                .isCreated()
                .expectBody(CommandeResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getReference()).isEqualTo("CMD-2026-001");
                    assertThat(commandeRepository.count()).isEqualTo(1);
                });
    }

    // =========================================================================
    // TEST 2 : Erreur - Client introuvable (404 Not Found)
    // =========================================================================
    @Test
    void cannotCreateCommandeIfClientNotFound() {
        // Given : Une requête avec un ID de client qui n'existe pas en base
        final CommandeCreationRequest request = CommandeCreationRequest.builder()
                .reference("CMD-ERR-404")
                .montant(100.0)
                .dateCommande(LocalDateTime.now())
                .clientId(999L) // ID fantôme
                .produitIds(Set.of())
                .build();

        // When
        webTestClient
                .post()
                .uri("/api/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                // Then
                .expectStatus()
                .isNotFound(); // S'assure que l'API renvoie bien 404 comme décrit dans le Swagger
    }

    // =========================================================================
    // TEST 3 : Erreur - Mauvaise requête (400 Bad Request)
    // =========================================================================
    @Test
    void cannotCreateCommandeWithBadRequest() {
        // Given : Pas de corps de requête

        // When
        webTestClient
                .post()
                .uri("/api/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus()
                .isBadRequest();
    }

    // =========================================================================
    // TEST 4 : Erreur - Récupérer une commande inexistante (404 Not Found)
    // =========================================================================
    @Test
    void getCommandeByIdNotFound() {
        // When : On essaie de récupérer la commande ID 999
        webTestClient
                .get()
                .uri("/api/commandes/{idCommande}", 999L)
                .exchange()
                // Then
                .expectStatus()
                .isNotFound();
    }

    // =========================================================================
    // TEST 5 : Récupérer toutes les commandes (200 OK)
    // =========================================================================
    @Test
    void canGetAllCommandes() {

        ClientEntity fauxClient = new ClientEntity();

        fauxClient = clientRepository.save(fauxClient);

        CommandeEntity commande1 = CommandeEntity.builder()
                .reference("CMD-ALL-1")
                .montant(50.0)
                .dateCommande(LocalDateTime.now())
                .clientEntity(fauxClient)
                .build();

        CommandeEntity commande2 = CommandeEntity.builder()
                .reference("CMD-ALL-2")
                .montant(75.5)
                .dateCommande(LocalDateTime.now())
                .clientEntity(fauxClient)
                .build();

        // On sauvegarde nos deux commandes directement dans la base H2
        commandeRepository.saveAll(java.util.List.of(commande1, commande2));

        // When : On appelle la route GET globale
        webTestClient
                .get()
                .uri("/api/commandes")
                .exchange()
                // Then
                .expectStatus()
                .isOk()

                .expectBodyList(CommandeResponse.class)
                .value(responses -> {
                    // On vérifie qu'on a bien récupéré exactement 2 commandes
                    assertThat(responses).hasSize(2);

                    // On peut même vérifier que nos références sont bien là
                    assertThat(responses)
                            .extracting(CommandeResponse::getReference)
                            .containsExactlyInAnyOrder("CMD-ALL-1", "CMD-ALL-2");
                });
    }
}