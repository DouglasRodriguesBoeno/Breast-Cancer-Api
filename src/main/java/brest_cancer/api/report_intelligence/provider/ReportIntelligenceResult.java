package brest_cancer.api.report_intelligence.provider;

import brest_cancer.api.report_intelligence.dto.ImportantTerm;
import brest_cancer.api.report_intelligence.dto.StructuredFindings;
import brest_cancer.api.report_intelligence.dto.WdbcCompatibility;

import java.util.List;

public record ReportIntelligenceResult(
        String detectedLanguage,
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
}
