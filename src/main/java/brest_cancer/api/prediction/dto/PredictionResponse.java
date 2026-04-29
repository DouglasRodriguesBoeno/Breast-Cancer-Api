package brest_cancer.api.prediction.dto;

import brest_cancer.api.prediction.persistence.entity.PredictionRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record PredictionResponse(
        Long id,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("predicted_label")
        String predictedLabel,

        @JsonProperty("predicted_label_name")
        String predictedLabelName,

        @JsonProperty("probability_malignant")
        double probabilityMalignant,

        @JsonProperty("probability_benign")
        double probabilityBenign,

        @JsonProperty("used_threshold_malignant")
        double usedThresholdMalignant,

        @JsonProperty("model_type")
        String modelType,

        @JsonProperty("risk_band")
        String riskBand,

        String summary,

        @JsonProperty("confidence_note")
        String confidenceNote,

        @JsonProperty("input_quality_note")
        String inputQualityNote,

        @JsonProperty("clinical_note")
        String clinicalNote,

        @JsonProperty("used_features")
        List<String> usedFeatures,

        @JsonProperty("ignored_features")
        List<String> ignoredFeatures,

        @JsonProperty("imputed_features")
        List<String> imputedFeatures,

        List<String> warnings
) {
    public static PredictionResponse from(PredictionRecord predictionRecord) {
        return new PredictionResponse(
                predictionRecord.getId(),
                predictionRecord.getCreatedAt(),
                predictionRecord.getPredictedLabel(),
                predictionRecord.getPredictedLabelName(),
                predictionRecord.getProbabilityMalignant(),
                predictionRecord.getProbabilityBenign(),
                predictionRecord.getUsedThresholdMalignant(),
                predictionRecord.getModelType(),
                predictionRecord.getRiskBand(),
                predictionRecord.getSummary(),
                predictionRecord.getConfidenceNote(),
                predictionRecord.getInputQualityNote(),
                predictionRecord.getClinicalNote(),
                predictionRecord.getUsedFeatures(),
                predictionRecord.getIgnoredFeatures(),
                predictionRecord.getImputedFeatures(),
                predictionRecord.getWarnings()
        );
    }
}