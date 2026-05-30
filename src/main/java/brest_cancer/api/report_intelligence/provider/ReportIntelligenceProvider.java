package brest_cancer.api.report_intelligence.provider;

import brest_cancer.api.report_intelligence.dto.AnalyzeReportRequest;

public interface ReportIntelligenceProvider {

    ReportIntelligenceResult analyze(AnalyzeReportRequest request);
}
