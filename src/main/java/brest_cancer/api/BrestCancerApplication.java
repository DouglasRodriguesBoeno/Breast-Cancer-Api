package brest_cancer.api;

import brest_cancer.api.report_intelligence.config.ReportIntelligenceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(ReportIntelligenceProperties.class)
@SpringBootApplication
public class BrestCancerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrestCancerApplication.class, args);
    }
}
