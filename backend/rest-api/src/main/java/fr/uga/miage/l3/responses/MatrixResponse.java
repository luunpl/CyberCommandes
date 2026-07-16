package fr.uga.miage.l3.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrixResponse {
    private List<List<Double>> distances;
    private List<List<Double>> times;
}