package brest_cancer.api.prediction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

@Schema(description = "Payload de entrada para criacao de uma nova analise")
public record PredictionRequest(
        @Schema(
                description = "Mapa com as features numericas esperadas pelo modelo",
                example = """
                        {
                          "radius_mean": 17.99,
                          "texture_mean": 10.38
                        }
                        """
        )
        @NotEmpty(message = "features nao pode ser vazio")
        Map<String, Double> features
) {
}