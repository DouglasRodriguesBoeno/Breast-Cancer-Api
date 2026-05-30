package brest_cancer.api.report_intelligence.provider;

import brest_cancer.api.report_intelligence.config.ReportIntelligenceProperties;
import brest_cancer.api.report_intelligence.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class LlmReportIntelligenceProvider implements ReportIntelligenceProvider {

    private static final String PROVIDER_NAME = "OPENAI_RESPONSES_API";

    private static final List<String> WDBC_FEATURE_NAMES = List.of(
            "radius_mean", "texture_mean", "perimeter_mean", "area_mean", "smoothness_mean",
            "compactness_mean", "concavity_mean", "concave_points_mean", "symmetry_mean", "fractal_dimension_mean",
            "radius_se", "texture_se", "perimeter_se", "area_se", "smoothness_se",
            "compactness_se", "concavity_se", "concave_points_se", "symmetry_se", "fractal_dimension_se",
            "radius_worst", "texture_worst", "perimeter_worst", "area_worst", "smoothness_worst",
            "compactness_worst", "concavity_worst", "concave_points_worst", "symmetry_worst", "fractal_dimension_worst"
    );

    private final ReportIntelligenceProperties reportIntelligenceProperties;
    private final ObjectMapper objectMapper;

    public LlmReportIntelligenceProvider(
            ReportIntelligenceProperties reportIntelligenceProperties,
            ObjectMapper objectMapper
    ) {
        this.reportIntelligenceProperties = reportIntelligenceProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public ReportIntelligenceResult analyze(AnalyzeReportRequest request) {
        ReportIntelligenceProperties.Llm llm = reportIntelligenceProperties.llmOrDefault();
        validateConfiguration(llm);

        Map<String, Object> requestBody = buildOpenAiRequest(request, llm);

        try {
            JsonNode response = RestClient.builder()
                    .baseUrl(llm.baseUrl())
                    .build()
                    .post()
                    .uri("/v1/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + llm.apiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "LLM report intelligence provider returned an empty response."
                );
            }

            String outputText = extractOutputText(response);
            LlmReportIntelligencePayload payload = objectMapper.readValue(
                    outputText,
                    LlmReportIntelligencePayload.class
            );

            return new ReportIntelligenceResult(
                    payload.detectedLanguage(),
                    payload.reportType(),
                    payload.structuredFindings(),
                    payload.importantTerms(),
                    payload.educationalSummary(),
                    payload.simpleExplanation(),
                    payload.wdbcCompatibility(),
                    payload.safetyNotes(),
                    PROVIDER_NAME,
                    llm.model()
            );
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to parse structured LLM report intelligence response.",
                    ex
            );
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to call LLM report intelligence provider.",
                    ex
            );
        }
    }

    private void validateConfiguration(ReportIntelligenceProperties.Llm llm) {
        if (isBlank(llm.baseUrl()) || isBlank(llm.apiKey()) || isBlank(llm.model())) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "LLM report intelligence provider is not configured. Set REPORT_INTELLIGENCE_LLM_BASE_URL, REPORT_INTELLIGENCE_LLM_API_KEY and REPORT_INTELLIGENCE_LLM_MODEL."
            );
        }
    }

    private Map<String, Object> buildOpenAiRequest(
            AnalyzeReportRequest request,
            ReportIntelligenceProperties.Llm llm
    ) {
        return linkedMap(
                "model", llm.model(),
                "input", List.of(
                        linkedMap(
                                "role", "system",
                                "content", buildSystemPrompt()
                        ),
                        linkedMap(
                                "role", "user",
                                "content", buildUserPrompt(request)
                        )
                ),
                "text", linkedMap(
                        "format", linkedMap(
                                "type", "json_schema",
                                "name", "report_intelligence_result",
                                "strict", true,
                                "schema", buildJsonSchema()
                        )
                )
        );
    }

    private String buildSystemPrompt() {
        return """
                You are the Laudo Intelligence Layer for BreastCare AI.

                Your job is to transform a breast-related medical report text into structured, multilingual, educational information.

                Safety rules:
                - Do not provide medical diagnosis.
                - Do not say the patient has cancer.
                - Do not recommend treatment.
                - Do not define clinical urgency.
                - Do not replace a healthcare professional.
                - Do not invent WDBC features.
                - Only mark WDBC compatibility as true if all 30 exact WDBC feature names are present in the provided text.
                - Use only information present in the report text.
                - If information is missing, use null or an empty list.
                - Keep explanations educational, calm, non-alarming and easy to understand.

                Required WDBC feature names:
                %s
                """.formatted(String.join(", ", WDBC_FEATURE_NAMES));
    }

    private String buildUserPrompt(AnalyzeReportRequest request) {
        return """
                Target language: %s
                Requested report type: %s
                Input type: %s

                Report text:
                %s
                """.formatted(
                request.targetLanguage(),
                request.reportType() == null || request.reportType().isBlank() ? "UNKNOWN" : request.reportType(),
                request.inputType(),
                request.reportText()
        );
    }

    private Map<String, Object> buildJsonSchema() {
        return objectSchema(
                linkedMap(
                        "detectedLanguage", stringEnum("pt-BR", "en", "es"),
                        "reportType", stringEnum("MAMMOGRAPHY", "ULTRASOUND", "MRI", "BIOPSY", "UNKNOWN"),
                        "structuredFindings", objectSchema(
                                linkedMap(
                                        "birads", nullableString(),
                                        "breastSide", nullableString(),
                                        "location", nullableString(),
                                        "measurements", arrayOf(objectSchema(
                                                linkedMap(
                                                        "value", linkedMap("type", "number"),
                                                        "unit", linkedMap("type", "string"),
                                                        "context", linkedMap("type", "string")
                                                ),
                                                "value", "unit", "context"
                                        )),
                                        "mentionedFindings", arrayOf(linkedMap("type", "string")),
                                        "mentionedRecommendations", arrayOf(linkedMap("type", "string"))
                                ),
                                "birads", "breastSide", "location", "measurements", "mentionedFindings", "mentionedRecommendations"
                        ),
                        "importantTerms", arrayOf(objectSchema(
                                linkedMap(
                                        "term", linkedMap("type", "string"),
                                        "explanation", linkedMap("type", "string")
                                ),
                                "term", "explanation"
                        )),
                        "educationalSummary", linkedMap("type", "string"),
                        "simpleExplanation", linkedMap("type", "string"),
                        "wdbcCompatibility", objectSchema(
                                linkedMap(
                                        "canRunPrediction", linkedMap("type", "boolean"),
                                        "reason", linkedMap("type", "string"),
                                        "detectedFeaturesCount", linkedMap("type", "integer"),
                                        "missingFeaturesCount", linkedMap("type", "integer"),
                                        "requiredFeaturesCount", linkedMap("type", "integer"),
                                        "detectedFeatureNames", arrayOf(linkedMap("type", "string"))
                                ),
                                "canRunPrediction", "reason", "detectedFeaturesCount", "missingFeaturesCount", "requiredFeaturesCount", "detectedFeatureNames"
                        ),
                        "safetyNotes", arrayOf(linkedMap("type", "string"))
                ),
                "detectedLanguage", "reportType", "structuredFindings", "importantTerms",
                "educationalSummary", "simpleExplanation", "wdbcCompatibility", "safetyNotes"
        );
    }

    private String extractOutputText(JsonNode response) {
        JsonNode outputText = response.path("output_text");
        if (outputText.isTextual() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        JsonNode output = response.path("output");
        if (output.isArray()) {
            for (JsonNode outputItem : output) {
                JsonNode content = outputItem.path("content");
                if (content.isArray()) {
                    for (JsonNode contentItem : content) {
                        JsonNode text = contentItem.path("text");
                        if (text.isTextual() && !text.asText().isBlank()) {
                            return text.asText();
                        }
                    }
                }
            }
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "LLM report intelligence provider did not return output text."
        );
    }

    private Map<String, Object> objectSchema(Map<String, Object> properties, String... required) {
        return linkedMap(
                "type", "object",
                "additionalProperties", false,
                "properties", properties,
                "required", List.of(required)
        );
    }

    private Map<String, Object> stringEnum(String... values) {
        return linkedMap(
                "type", "string",
                "enum", List.of(values)
        );
    }

    private Map<String, Object> nullableString() {
        return linkedMap(
                "type", List.of("string", "null")
        );
    }

    private Map<String, Object> arrayOf(Map<String, Object> items) {
        return linkedMap(
                "type", "array",
                "items", items
        );
    }

    private Map<String, Object> linkedMap(Object... entries) {
        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException("linkedMap requires an even number of arguments.");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            map.put((String) entries[index], entries[index + 1]);
        }
        return map;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record LlmReportIntelligencePayload(
            String detectedLanguage,
            String reportType,
            StructuredFindings structuredFindings,
            List<ImportantTerm> importantTerms,
            String educationalSummary,
            String simpleExplanation,
            WdbcCompatibility wdbcCompatibility,
            List<String> safetyNotes
    ) {
    }
}
