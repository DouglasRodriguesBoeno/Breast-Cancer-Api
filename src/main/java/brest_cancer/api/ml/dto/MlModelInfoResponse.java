package brest_cancer.api.ml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record MlModelInfoResponse(
        @JsonProperty("model_type")
        String modelType,

        @JsonProperty("trained_at")
        String trainedAt,

        @JsonProperty("accuracy_test")
        double accuracyTest,

        @JsonProperty("threshold_malignant")
        double thresholdMalignant,

        String notes,
        Map<String, Object> extra
) {
}