package brest_cancer.api.report_intelligence.provider;

import brest_cancer.api.report_intelligence.config.ReportIntelligenceProperties;
import brest_cancer.api.report_intelligence.dto.AnalyzeReportRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LlmReportIntelligenceProvider implements ReportIntelligenceProvider {

    private final ReportIntelligenceProperties reportIntelligenceProperties;

    public LlmReportIntelligenceProvider(ReportIntelligenceProperties reportIntelligenceProperties) {
        this.reportIntelligenceProperties = reportIntelligenceProperties;
    }

    @Override
    public ReportIntelligenceResult analyze(AnalyzeReportRequest request) {
        ReportIntelligenceProperties.Llm llm = reportIntelligenceProperties.llmOrDefault();

        if (isBlank(llm.baseUrl()) || isBlank(llm.apiKey()) || isBlank(llm.model())) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "LLM report intelligence provider is not configured. Set REPORT_INTELLIGENCE_LLM_BASE_URL, REPORT_INTELLIGENCE_LLM_API_KEY and REPORT_INTELLIGENCE_LLM_MODEL."
            );
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "LLM report intelligence provider is configured but not implemented yet."
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
