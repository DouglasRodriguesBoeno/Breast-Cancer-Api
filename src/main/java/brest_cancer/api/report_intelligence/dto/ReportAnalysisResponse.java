package brest_cancer.api.report_intelligence.dto;

import brest_cancer.api.report_intelligence.persistence.entity.ReportAnalysisRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record ReportAnalysisResponse(
        Long id,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        String status,
        String inputType,
        String detectedLanguage,
        String targetLanguage,
        String reportType,
        StructuredFindings structuredFindings,
        List<ImportantTerm> importantTerms,
        String educationalSummary,
        String simpleExplanation,
        WdbcCompatibility wdbcCompatibility,
        List<String> safetyNotes,
        String provider,
        String providerModel
) {
    public static ReportAnalysisResponse from(ReportAnalysisRecord record) {
        return new ReportAnalysisResponse(
                record.getId(),
                record.getCreatedAt(),
                record.getProcessingStatus(),
                record.getInputType(),
                record.getDetectedLanguage(),
                record.getTargetLanguage(),
                record.getReportType(),
                record.getStructuredFindings(),
                record.getImportantTerms(),
                record.getEducationalSummary(),
                record.getSimpleExplanation(),
                record.getWdbcCompatibility(),
                record.getSafetyNotes(),
                record.getProvider(),
                record.getProviderModel()
        );
    }
}
