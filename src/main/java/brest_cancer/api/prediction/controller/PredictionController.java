package brest_cancer.api.prediction.controller;

import brest_cancer.api.prediction.dto.PredictionRequest;
import brest_cancer.api.prediction.dto.PredictionResponse;
import brest_cancer.api.prediction.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public ResponseEntity<PredictionResponse> createPrediction(
            @RequestBody @Valid PredictionRequest request
    ) {
        return ResponseEntity.ok(predictionService.createPrediction(request));
    }
}
