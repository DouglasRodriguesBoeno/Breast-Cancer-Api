package brest_cancer.api.ml.dto;

public record MlHealthResponse(
    String status,
    boolean modelLoaded,
    boolean predictReady
) {
}
