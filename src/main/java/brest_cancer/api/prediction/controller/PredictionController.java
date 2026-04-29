package brest_cancer.api.prediction.controller;

import brest_cancer.api.prediction.dto.PredictionListItemResponse;
import brest_cancer.api.prediction.dto.PredictionRequest;
import brest_cancer.api.prediction.dto.PredictionResponse;
import brest_cancer.api.prediction.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<PredictionListItemResponse>> findAll() {
        return ResponseEntity.ok(predictionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PredictionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(predictionService.findById(id));
    }
}
