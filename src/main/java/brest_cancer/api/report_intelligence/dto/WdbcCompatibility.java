package brest_cancer.api.report_intelligence.dto;

import java.util.List;

public record WdbcCompatibility(
        boolean canRunPrediction,
        String reason,
        int detectedFeaturesCount,
        int missingFeaturesCount,
        int requiredFeaturesCount,
        List<String> detectedFeatureNames
) {
}
