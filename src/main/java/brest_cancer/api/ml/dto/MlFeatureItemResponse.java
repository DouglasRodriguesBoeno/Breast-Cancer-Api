package brest_cancer.api.ml.dto;

public record MlFeatureItemResponse(
        String name,
        double mean,
        double std,
        double min,
        double max
) {
}
