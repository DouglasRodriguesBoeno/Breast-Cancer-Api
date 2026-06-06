package brest_cancer.api.report_intelligence.provider;

import brest_cancer.api.report_intelligence.config.ReportIntelligenceProperties;
import brest_cancer.api.report_intelligence.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
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
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to call LLM report intelligence provider.",
                    ex
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to parse structured LLM report intelligence response.",
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

                Your job is to transform breast-related report text or sparse user-provided breast information into structured, multilingual, educational information.

                Product goal:
                - Help lay users understand what was informed, what is still missing, what cannot be concluded, and how to prepare a better conversation with a healthcare professional.
                - The output must feel useful, caring, realistic and practical, especially when the user has little information.
                - Do not only define medical terms. Organize uncertainty into clear educational guidance.

                Safety rules:
                - Do not provide medical diagnosis.
                - Do not say the patient has cancer.
                - Do not say the patient does not have cancer.
                - Do not recommend treatment.
                - Do not define clinical urgency.
                - Do not replace a healthcare professional.
                - Do not invent WDBC features.
                - Do not create personalized cancer percentages or probabilities.
                - Do not apply general statistics to the individual user.
                - Only mark WDBC compatibility as true if all 30 exact WDBC feature names are present in the provided text.
                - Use only information present in the report text.
                - If structured clinical information is missing, use null or an empty list in structuredFindings.
                - Keep explanations educational, calm, non-alarming and easy to understand.
                - Avoid definitive reassurance such as "not dangerous", "safe", "no risk", "low risk", or equivalent wording as a conclusion for the user.
                - Prefer wording such as "the text mentions", "the report describes", "described as probably benign", "requires follow-up according to medical guidance", and "should be interpreted by a healthcare professional".
                - When a BI-RADS category is present, output it in the normalized format "BI-RADS X".

                Sparse or incomplete input behavior:
                When the input is sparse, incomplete, informal, symptom-like, or not a full medical report, do not return a short generic answer.
                Instead, still produce a complete and helpful educational response using the existing response fields.

                For sparse input:
                - structuredFindings must only contain facts explicitly present in the text.
                - educationalSummary should be rich and organized. Include short labeled sections in the target language:
                  1. What was informed.
                  2. What is still missing.
                  3. What cannot be concluded.
                  4. What information could help complete the analysis.
                - simpleExplanation should explain the situation in plain language and focus on organizing uncertainty, not just defining a nodule or term.
                - importantTerms should include useful terms relevant to the missing context when appropriate, such as nodule, BI-RADS, margins, measurements, follow-up, mammography, ultrasound, biopsy or WDBC. Explain them in simple, non-diagnostic language.
                - safetyNotes should include practical, safe reminders and useful non-diagnostic questions to ask a healthcare professional.
                - wdbcCompatibility must explain that sparse text does not contain the 30 numerical WDBC features.

                Useful guidance for sparse input may include:
                - Ask whether the finding has a BI-RADS category.
                - Ask the size of the nodule or finding.
                - Ask whether margins or contours were described.
                - Ask which exam identified the finding: mammography, ultrasound, MRI, biopsy or physical exam.
                - Ask whether the report recommends follow-up, comparison or complementary imaging.
                - Suggest that the user can write down when the finding was noticed, whether it changed, whether there is pain, discharge, skin change, nipple change, cycle-related change or relevant family history.
                - Clearly state that these notes do not determine diagnosis, but may help the healthcare professional understand the context.

                General education rules:
                - You may provide general educational context, such as that breast changes can have different causes and should be evaluated by a healthcare professional.
                - You may mention general care topics such as healthy weight, physical activity, limiting alcohol, knowing family history and screening according to professional guidance.
                - Always state that general care guidance does not guarantee prevention and does not replace medical evaluation.
                - Do not include unsourced exact percentages.

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
        if (outputText.isString() && !outputText.asString().isBlank()) {
            return outputText.asString();
        }

        JsonNode output = response.path("output");
        if (output.isArray()) {
            for (JsonNode outputItem : output) {
                JsonNode content = outputItem.path("content");
                if (content.isArray()) {
                    for (JsonNode contentItem : content) {
                        JsonNode text = contentItem.path("text");
                        if (text.isString() && !text.asString().isBlank()) {
                            return text.asString();
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
