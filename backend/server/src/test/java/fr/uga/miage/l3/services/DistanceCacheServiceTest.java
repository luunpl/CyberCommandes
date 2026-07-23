package fr.uga.miage.l3.services;

import fr.uga.miage.l3.models.DistanceCacheEntity;
import fr.uga.miage.l3.repository.DistanceCacheRepository;
import fr.uga.miage.l3.request.CoordinateRequest;
import fr.uga.miage.l3.responses.MatrixResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DistanceCacheServiceTest {

    private DistanceCacheService service;

    private final CoordinateRequest a = new CoordinateRequest(45.1, 5.7);
    private final CoordinateRequest b = new CoordinateRequest(45.2, 5.8);
    private final CoordinateRequest c = new CoordinateRequest(45.3, 5.9);

    private DistanceCacheEntity trajet(CoordinateRequest from, CoordinateRequest to, double km) {
        return new DistanceCacheEntity(null, from.lat(), from.lng(), to.lat(), to.lng(), km, km / 30 * 3600);
    }

    @BeforeEach
    void setUp() {
        DistanceCacheRepository repository = mock(DistanceCacheRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        service = new DistanceCacheService(repository);
        service.initCache();
    }

    @Test
    @DisplayName("complete cache -> full matrix with real values")
    void buildsMatrixWhenCacheIsComplete() {
        List<CoordinateRequest> points = List.of(a, b, c);
        for (CoordinateRequest from : points) {
            for (CoordinateRequest to : points) {
                if (from != to) service.saveTrajet(trajet(from, to, 5.0));
            }
        }

        MatrixResponse matrix = service.buildMatrixIfComplete(points);

        assertThat(matrix).isNotNull();
        assertThat(matrix.getDistances()).hasSize(3);
        assertThat(matrix.getDistances().get(0).get(0)).isZero();
        assertThat(matrix.getDistances().get(0).get(1)).isEqualTo(5.0);
    }

    @Test
    @DisplayName("mostly empty cache -> null so the caller falls back to ORS")
    void returnsNullWhenCacheIsMostlyMissing() {
        assertThat(service.buildMatrixIfComplete(List.of(a, b, c))).isNull();
    }

    @Test
    @DisplayName("saveTrajet is idempotent per coordinate pair")
    void doesNotDuplicateCachedPairs() {
        service.saveTrajet(trajet(a, b, 5.0));
        service.saveTrajet(trajet(a, b, 7.0)); // same pair, must keep the first entry

        assertThat(service.getAllTrajets()).hasSize(1);
        assertThat(service.getTrajet(a.lat(), a.lng(), b.lat(), b.lng()).getDistance()).isEqualTo(5.0);
    }
}
