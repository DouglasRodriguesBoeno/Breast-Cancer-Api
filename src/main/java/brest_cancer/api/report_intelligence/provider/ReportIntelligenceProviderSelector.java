package brest_cancer.api.report_intelligence.provider;

import brest_cancer.api.report_intelligence.config.ReportIntelligenceProperties;
import brest_cancer.api.report_intelligence.dto.AnalyzeReportRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ReportIntelligenceProviderSelector implements ReportIntelligenceProvider {

    private final ReportIntelligenceProperties reportIntelligenceProperties;
    private final RuleBasedReportIntelligenceProvider ruleBasedReportIntelligenceProvider;
    private final LlmReportIntelligenceProvider llmReportIntelligenceProvider;

    public ReportIntelligenceProviderSelector(
            ReportIntelligenceProperties reportIntelligenceProperties,
            RuleBasedReportIntelligenceProvider ruleBasedReportIntelligenceProvider,
            LlmReportIntelligenceProvider llmReportIntelligenceProvider
    ) {
        this.reportIntelligenceProperties = reportIntelligenceProperties;
        this.ruleBasedReportIntelligenceProvider = ruleBasedReportIntelligenceProvider;
        this.llmReportIntelligenceProvider = llmReportIntelligenceProvider;
    }

    @Override
    public ReportIntelligenceResult analyze(AnalyzeReportRequest request) {
        return switch (reportIntelligenceProperties.providerOrDefault()) {
            case RULE_BASED -> ruleBasedReportIntelligenceProvider.analyze(request);
            case LLM -> llmReportIntelligenceProvider.analyze(request);
        };
    }
}
