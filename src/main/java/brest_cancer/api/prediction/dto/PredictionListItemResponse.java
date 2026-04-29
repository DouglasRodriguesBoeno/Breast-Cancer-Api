package brest_cancer.api.prediction.dto;

import brest_cancer.api.prediction.persistence.entity.PredictionRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record PredictionListItemResponse(
        Long id,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("predicted_label")
        String predictedLabel,

        @JsonProperty("predicted_label_name")
        String predictedLabelName,

        @JsonProperty("probability_malignant")
        double probabilityMalignant,

        @JsonProperty("risk_band")
        String riskBand,

        @JsonProperty("model_type")
        String modelType
) {
    public static PredictionListItemResponse from(PredictionRecord predictionRecord) {
        return new PredictionListItemResponse(
                predictionRecord.getId(),
                predictionRecord.getCreatedAt(),
                predictionRecord.getPredictedLabel(),
                predictionRecord.getPredictedLabelName(),
                predictionRecord.getProbabilityMalignant(),
                predictionRecord.getRiskBand(),
                predictionRecord.getModelType()
        );
    }
}