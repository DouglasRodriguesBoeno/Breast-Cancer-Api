package brest_cancer.api.ml.controller;

import brest_cancer.api.ml.client.MlServiceClient;
import brest_cancer.api.ml.dto.MlFeaturesResponse;
import brest_cancer.api.ml.dto.MlHealthResponse;
import brest_cancer.api.ml.dto.MlModelInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ml")
public class MlIntegrationController {

    private final MlServiceClient mlServiceClient;

    public MlIntegrationController(MlServiceClient mlServiceClient) {
        this.mlServiceClient = mlServiceClient;
    }

    @GetMapping("/health")
    public ResponseEntity<MlHealthResponse> health() {
        return ResponseEntity.ok(mlServiceClient.getHealth());
    }

    @GetMapping("/features")
    public ResponseEntity<MlFeaturesResponse> features() {
        return ResponseEntity.ok(mlServiceClient.getFeatures());
    }

    @GetMapping("/model-info")
    public ResponseEntity<MlModelInfoResponse> modelInfo() {
        return ResponseEntity.ok(mlServiceClient.getModelInfo());
    }
}
