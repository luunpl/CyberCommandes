package fr.uga.miage.l3.services;

import fr.uga.miage.l3.models.DistanceCacheEntity;
import fr.uga.miage.l3.repository.DistanceCacheRepository;
import fr.uga.miage.l3.request.CoordinateRequest;
import fr.uga.miage.l3.responses.MatrixResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistanceCacheService {
    private final DistanceCacheRepository distanceCacheRepository;

    //  LE CACHE EN RAM ! (Ultra-rapide, prend très peu de place)
    private final Map<String, DistanceCacheEntity> ramCache = new ConcurrentHashMap<>();

    // Générateur de clé unique pour chaque trajet
    private String generateKey(Double latDep, Double lngDep, Double latArr, Double lngArr) {
        return round(latDep) + "_" + round(lngDep) + "_" + round(latArr) + "_" + round(lngArr);
    }

    // Petite fonction utilitaire pour arrondir proprement
    private double round(Double value) {
        if (value == null) return 0.0;
        return Math.round(value * 1000000.0) / 1000000.0;
    }

    // Cette méthode s'exécute toute seule au démarrage de Spring Boot
    @PostConstruct
    public void initCache() {
        log.info("Chargement du cache de distances en RAM...");
        List<DistanceCacheEntity> all = distanceCacheRepository.findAll();
        for (DistanceCacheEntity entity : all) {
            ramCache.put(generateKey(entity.getLatDepart(), entity.getLngDepart(), entity.getLatArrivee(), entity.getLngArrivee()), entity);
        }
        log.info("{} trajets charges en RAM", all.size());
    }

    public void saveAllTrajets(List<DistanceCacheEntity> trajets) {
        List<DistanceCacheEntity> nouveauxTrajets = new ArrayList<>();

        for (DistanceCacheEntity trajet : trajets) {
            String key = generateKey(trajet.getLatDepart(), trajet.getLngDepart(), trajet.getLatArrivee(), trajet.getLngArrivee());

            // Si on ne l'a pas en RAM, on l'ajoute à la liste pour le sauvegarder
            if (!ramCache.containsKey(key)) {
                nouveauxTrajets.add(trajet);
                ramCache.put(key, trajet); // On met à jour la RAM tout de suite
            }
        }

        // On sauvegarde TOUT d'un coup dans la BDD (100x plus rapide qu'une boucle)
        if (!nouveauxTrajets.isEmpty()) {
            distanceCacheRepository.saveAll(nouveauxTrajets);
        }
    }

    public void saveTrajet(DistanceCacheEntity trajet) {
        String key = generateKey(trajet.getLatDepart(), trajet.getLngDepart(), trajet.getLatArrivee(), trajet.getLngArrivee());
        if (!ramCache.containsKey(key)) {
            distanceCacheRepository.save(trajet);
            ramCache.put(key, trajet);
        }
    }

    public DistanceCacheEntity getTrajet(Double latDep, Double lngDep, Double latArr, Double lngArr) {
        // 🚀 RECHERCHE INSTANTANÉE DANS LA RAM (0 milliseconde au lieu de 10ms)
        String key = generateKey(latDep, lngDep, latArr, lngArr);
        return ramCache.get(key);
    }

    public List<DistanceCacheEntity> getAllTrajets() {
        return new ArrayList<>(ramCache.values());
    }

    /**
     * Builds the full distance/time matrix for the given points from the cache.
     * A few missing pairs (max 5%) are tolerated and filled with a prohibitive
     * 9999 value so the algorithms simply avoid them. Beyond that threshold the
     * cache is considered incomplete and null is returned, letting the caller
     * (REST 404) trigger the frontend's OpenRouteService fallback.
     */
    public MatrixResponse buildMatrixIfComplete(List<CoordinateRequest> points) {
        int n = points.size();
        List<List<Double>> distances = new ArrayList<>();
        List<List<Double>> times = new ArrayList<>();
        int missing = 0;

        for (int i = 0; i < n; i++) {
            List<Double> distRow = new ArrayList<>();
            List<Double> timeRow = new ArrayList<>();

            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distRow.add(0.0);
                    timeRow.add(0.0);
                } else {
                    CoordinateRequest p1 = points.get(i);
                    CoordinateRequest p2 = points.get(j);

                    DistanceCacheEntity trajet = getTrajet(p1.lat(), p1.lng(), p2.lat(), p2.lng());

                    if (trajet == null) {
                        missing++;
                        log.warn("Trajet manquant ignore : ({}, {}) vers ({}, {})", p1.lat(), p1.lng(), p2.lat(), p2.lng());
                        distRow.add(9999.0);
                        timeRow.add(9999.0);
                    } else {
                        distRow.add(trajet.getDistance());
                        timeRow.add(trajet.getTemps());
                    }
                }
            }
            distances.add(distRow);
            times.add(timeRow);
        }

        int totalPairs = n * (n - 1);
        if (totalPairs > 0 && missing > totalPairs * 0.05) {
            log.info("Cache incomplet : {}/{} trajets manquants, matrice non servie", missing, totalPairs);
            return null;
        }
        return new MatrixResponse(distances, times);
    }
}
