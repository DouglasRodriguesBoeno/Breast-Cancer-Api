package brest_cancer.api.ml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MlPredictResponse(
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
}