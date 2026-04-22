package brest_cancer.api.prediction.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public record PredictionRequest(
        @NotEmpty(message = "features nao pode ser vazio")
        Map<String, Double> features
) {
}
