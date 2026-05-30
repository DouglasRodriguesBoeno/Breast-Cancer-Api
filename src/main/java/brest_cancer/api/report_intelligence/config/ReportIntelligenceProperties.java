package brest_cancer.api.report_intelligence.config;

import brest_cancer.api.report_intelligence.provider.ReportIntelligenceProviderType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "report-intelligence")
public record ReportIntelligenceProperties(
        ReportIntelligenceProviderType provider,
        Llm llm
) {
    public ReportIntelligenceProviderType providerOrDefault() {
        return provider == null ? ReportIntelligenceProviderType.RULE_BASED : provider;
    }

    public Llm llmOrDefault() {
        return llm == null ? new Llm(null, null, null, 10000) : llm;
    }

    public record Llm(
            String baseUrl,
            String apiKey,
            String model,
            Integer timeoutMs
    ) {
        public int timeoutMsOrDefault() {
            return timeoutMs == null ? 10000 : timeoutMs;
        }
    }
}
