package fr.uga.miage.l3.responses;

import lombok.Data;

@Data
public class DistanceCacheResponse {
    private Double latDepart;
    private Double lngDepart;
    private Double latArrivee;
    private Double lngArrivee;
    private Double distance;
    private Double temps;
}