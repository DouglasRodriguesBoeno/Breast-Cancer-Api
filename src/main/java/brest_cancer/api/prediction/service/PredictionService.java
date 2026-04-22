package brest_cancer.api.prediction.service;

import brest_cancer.api.ml.client.MlServiceClient;
import brest_cancer.api.ml.dto.MlPredictRequest;
import brest_cancer.api.ml.dto.MlPredictResponse;
import brest_cancer.api.prediction.dto.PredictionRequest;
import brest_cancer.api.prediction.dto.PredictionResponse;
import org.springframework.stereotype.Service;

@Service
public class PredictionService {

    private final MlServiceClient mlServiceClient;

    public PredictionService(MlServiceClient mlServiceClient) {
        this.mlServiceClient = mlServiceClient;
    }

    public PredictionResponse createPrediction(PredictionRequest request) {
        MlPredictResponse mlResponse = mlServiceClient.predict(
                new MlPredictRequest(request.features())
        );

        return PredictionResponse.from(mlResponse);
    }
}
