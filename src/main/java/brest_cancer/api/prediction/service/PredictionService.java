package brest_cancer.api.prediction.service;

import brest_cancer.api.ml.client.MlServiceClient;
import brest_cancer.api.ml.dto.MlPredictRequest;
import brest_cancer.api.ml.dto.MlPredictResponse;
import brest_cancer.api.prediction.dto.PredictionRequest;
import brest_cancer.api.prediction.dto.PredictionResponse;
import brest_cancer.api.prediction.persistence.entity.PredictionRecord;
import brest_cancer.api.prediction.persistence.repository.PredictionRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictionService {

    private final MlServiceClient mlServiceClient;
    private final PredictionRecordRepository predictionRecordRepository;

    public PredictionService(
            MlServiceClient mlServiceClient,
            PredictionRecordRepository predictionRecordRepository
    ) {
        this.mlServiceClient = mlServiceClient;
        this.predictionRecordRepository = predictionRecordRepository;
    }

    @Transactional
    public PredictionResponse createPrediction(PredictionRequest request) {
        MlPredictResponse mlResponse = mlServiceClient.predict(
                new MlPredictRequest(request.features())
        );

        PredictionRecord predictionRecord = new PredictionRecord();
        predictionRecord.setInputFeatures(request.features());
        predictionRecord.setPredictedLabel(mlResponse.predictedLabel());
        predictionRecord.setPredictedLabelName(mlResponse.predictedLabelName());
        predictionRecord.setProbabilityMalignant(mlResponse.probabilityMalignant());
        predictionRecord.setProbabilityBenign(mlResponse.probabilityBenign());
        predictionRecord.setUsedThresholdMalignant(mlResponse.usedThresholdMalignant());
        predictionRecord.setModelType(mlResponse.modelType());
        predictionRecord.setRiskBand(mlResponse.riskBand());
        predictionRecord.setSummary(mlResponse.summary());
        predictionRecord.setConfidenceNote(mlResponse.confidenceNote());
        predictionRecord.setInputQualityNote(mlResponse.inputQualityNote());
        predictionRecord.setClinicalNote(mlResponse.clinicalNote());
        predictionRecord.setUsedFeatures(mlResponse.usedFeatures());
        predictionRecord.setIgnoredFeatures(mlResponse.ignoredFeatures());
        predictionRecord.setImputedFeatures(mlResponse.imputedFeatures());
        predictionRecord.setWarnings(mlResponse.warnings());

        predictionRecordRepository.save(predictionRecord);

        return PredictionResponse.from(mlResponse);
    }
}