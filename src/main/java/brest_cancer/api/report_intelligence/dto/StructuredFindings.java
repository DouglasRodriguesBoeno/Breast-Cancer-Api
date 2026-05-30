package brest_cancer.api.report_intelligence.dto;

import java.util.List;

public record StructuredFindings(
        String birads,
        String breastSide,
        String location,
        List<ReportMeasurement> measurements,
        List<String> mentionedFindings,
        List<String> mentionedRecommendations
) {
}
