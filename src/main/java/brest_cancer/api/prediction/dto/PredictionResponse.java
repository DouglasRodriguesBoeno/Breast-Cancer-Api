package brest_cancer.api.prediction.dto;

import brest_cancer.api.ml.dto.MlPredictResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PredictionResponse(
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
    public static PredictionResponse from(MlPredictResponse mlResponse) {
        return new PredictionResponse(
                mlResponse.predictedLabel(),
                mlResponse.predictedLabelName(),
                mlResponse.probabilityMalignant(),
                mlResponse.probabilityBenign(),
                mlResponse.usedThresholdMalignant(),
                mlResponse.modelType(),
                mlResponse.riskBand(),
                mlResponse.summary(),
                mlResponse.confidenceNote(),
                mlResponse.inputQualityNote(),
                mlResponse.clinicalNote(),
                mlResponse.usedFeatures(),
                mlResponse.ignoredFeatures(),
                mlResponse.imputedFeatures(),
                mlResponse.warnings()
        );
    }
}