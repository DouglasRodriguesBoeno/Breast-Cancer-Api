package brest_cancer.api.prediction.controller;

import brest_cancer.api.common.dto.ApiErrorResponse;
import brest_cancer.api.prediction.dto.PredictionListItemResponse;
import brest_cancer.api.prediction.dto.PredictionRequest;
import brest_cancer.api.prediction.dto.PredictionResponse;
import brest_cancer.api.prediction.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@Tag(name = "Predictions", description = "Endpoints para criacao, listagem e detalhamento de analises")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    @Operation(
            summary = "Criar nova analise",
            description = "Recebe um conjunto de features, consulta o ML Service, persiste a analise no banco e retorna o resultado completo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Analise criada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PredictionResponse.class),
                            examples = @ExampleObject(
                                    name = "prediction-created",
                                    value = """
                                            {
                                              "id": 2,
                                              "created_at": "2026-04-29T19:57:53.597746-03:00",
                                              "predicted_label": "M",
                                              "predicted_label_name": "Maligno",
                                              "probability_malignant": 0.9733333236663441,
                                              "probability_benign": 0.026666676333655933,
                                              "used_threshold_malignant": 0.34331160756760615,
                                              "model_type": "ensemble_mean_logistic_random_forest",
                                              "risk_band": "high",
                                              "summary": "Os dados informados apresentam maior compatibilidade com padrao maligno segundo o modelo treinado.",
                                              "confidence_note": "O resultado ficou bem distante do threshold configurado, indicando maior separacao probabilistica entre as classes.",
                                              "input_quality_note": "Todos os campos utilizados foram fornecidos conforme esperado pelo modelo.",
                                              "clinical_note": "Resultado educacional e probabilistico. Nao substitui avaliacao medica, exames complementares ou diagnostico clinico.",
                                              "used_features": [
                                                "radius_mean",
                                                "texture_mean"
                                              ],
                                              "ignored_features": [],
                                              "imputed_features": [],
                                              "warnings": [
                                                "Modelo treinado para fins educacionais. Nao usar para diagnostico clinico."
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada invalidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "validation-error",
                                    value = """
                                            {
                                              "timestamp": "2026-04-29T20:30:00-03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "features: features nao pode ser vazio",
                                              "path": "/api/predictions"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Falha ao consultar o ML Service",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ml-unavailable",
                                    value = """
                                            {
                                              "timestamp": "2026-04-29T20:32:00-03:00",
                                              "status": 502,
                                              "error": "Bad Gateway",
                                              "message": "Falha ao consultar o ML Service. Verifique se o servico de inferencia esta disponivel.",
                                              "path": "/api/predictions"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<PredictionResponse> createPrediction(
            @RequestBody(
                    required = true,
                    description = "Payload com as features numericas usadas para a predicao",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PredictionRequest.class),
                            examples = @ExampleObject(
                                    name = "prediction-request",
                                    value = """
                                            {
                                              "features": {
                                                "radius_mean": 17.99,
                                                "texture_mean": 10.38,
                                                "perimeter_mean": 122.8,
                                                "area_mean": 1001.0,
                                                "smoothness_mean": 0.1184,
                                                "compactness_mean": 0.2776,
                                                "concavity_mean": 0.3001,
                                                "concave_points_mean": 0.1471,
                                                "symmetry_mean": 0.2419,
                                                "fractal_dimension_mean": 0.07871,
                                                "radius_se": 1.095,
                                                "texture_se": 0.9053,
                                                "perimeter_se": 8.589,
                                                "area_se": 153.4,
                                                "smoothness_se": 0.006399,
                                                "compactness_se": 0.04904,
                                                "concavity_se": 0.05373,
                                                "concave_points_se": 0.01587,
                                                "symmetry_se": 0.03003,
                                                "fractal_dimension_se": 0.006193,
                                                "radius_worst": 25.38,
                                                "texture_worst": 17.33,
                                                "perimeter_worst": 184.6,
                                                "area_worst": 2019.0,
                                                "smoothness_worst": 0.1622,
                                                "compactness_worst": 0.6656,
                                                "concavity_worst": 0.7119,
                                                "concave_points_worst": 0.2654,
                                                "symmetry_worst": 0.4601,
                                                "fractal_dimension_worst": 0.1189
                                              }
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid PredictionRequest request
    ) {
        return ResponseEntity.ok(predictionService.createPrediction(request));
    }

    @GetMapping
    @Operation(
            summary = "Listar historico de analises",
            description = "Retorna o historico resumido de analises ordenado por data de criacao decrescente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Historico retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PredictionListItemResponse.class),
                            examples = @ExampleObject(
                                    name = "prediction-history",
                                    value = """
                                            [
                                              {
                                                "id": 2,
                                                "created_at": "2026-04-29T20:00:00-03:00",
                                                "predicted_label": "M",
                                                "predicted_label_name": "Maligno",
                                                "probability_malignant": 0.91,
                                                "risk_band": "high",
                                                "model_type": "ensemble_mean_logistic_random_forest"
                                              },
                                              {
                                                "id": 1,
                                                "created_at": "2026-04-29T19:00:00-03:00",
                                                "predicted_label": "B",
                                                "predicted_label_name": "Benigno",
                                                "probability_malignant": 0.10,
                                                "risk_band": "low",
                                                "model_type": "ensemble_mean_logistic_random_forest"
                                              }
                                            ]
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<List<PredictionListItemResponse>> findAll() {
        return ResponseEntity.ok(predictionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar detalhe de uma analise",
            description = "Retorna os detalhes completos de uma analise a partir do id."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Analise encontrada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PredictionResponse.class),
                            examples = @ExampleObject(
                                    name = "prediction-detail",
                                    value = """
                                            {
                                              "id": 2,
                                              "created_at": "2026-04-29T19:57:53.597746-03:00",
                                              "predicted_label": "M",
                                              "predicted_label_name": "Maligno",
                                              "probability_malignant": 0.9733333236663441,
                                              "probability_benign": 0.026666676333655933,
                                              "used_threshold_malignant": 0.34331160756760615,
                                              "model_type": "ensemble_mean_logistic_random_forest",
                                              "risk_band": "high",
                                              "summary": "Os dados informados apresentam maior compatibilidade com padrao maligno segundo o modelo treinado.",
                                              "confidence_note": "O resultado ficou bem distante do threshold configurado, indicando maior separacao probabilistica entre as classes.",
                                              "input_quality_note": "Todos os campos utilizados foram fornecidos conforme esperado pelo modelo.",
                                              "clinical_note": "Resultado educacional e probabilistico. Nao substitui avaliacao medica, exames complementares ou diagnostico clinico.",
                                              "used_features": [
                                                "radius_mean",
                                                "texture_mean"
                                              ],
                                              "ignored_features": [],
                                              "imputed_features": [],
                                              "warnings": [
                                                "Modelo treinado para fins educacionais. Nao usar para diagnostico clinico."
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Analise nao encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "prediction-not-found",
                                    value = """
                                            {
                                              "timestamp": "2026-04-29T20:31:00-03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Analise nao encontrada para o id: 999",
                                              "path": "/api/predictions/999"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<PredictionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(predictionService.findById(id));
    }
}