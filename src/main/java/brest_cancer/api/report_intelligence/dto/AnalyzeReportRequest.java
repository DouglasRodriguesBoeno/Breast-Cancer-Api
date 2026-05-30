package brest_cancer.api.report_intelligence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tools.jackson.annotation.JsonProperty;

public record AnalyzeReportRequest(
        @NotBlank(message = "inputType e obrigatorio")
        String inputType,

        @NotBlank(message = "targetLanguage e obrigatorio")
        String targetLanguage,

        @NotBlank(message = "reportText e obrigatorio")
        @Size(max = 50000, message = "reportText deve ter no maximo 50000 caracteres")
        String reportText,

        String reportType,

        @JsonProperty("persistRawText")
        Boolean persistRawText
) {
}
