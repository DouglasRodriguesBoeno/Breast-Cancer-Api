package brest_cancer.api.report_intelligence.dto;

public record ReportMeasurement(
        double value,
        String unit,
        String context
) {
}
