package brest_cancer.api.ml.dto;

import java.util.List;

public record MlFeatureResponse(
        List<MlFeatureResponse> features
) {
}
