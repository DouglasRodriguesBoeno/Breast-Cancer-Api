package brest_cancer.api.report_intelligence.controller;

import brest_cancer.api.common.dto.ApiErrorResponse;
import brest_cancer.api.report_intelligence.dto.AnalyzeReportRequest;
import brest_cancer.api.report_intelligence.dto.ReportAnalysisResponse;
import brest_cancer.api.report_intelligence.service.ReportIntelligenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report-intelligence")
@Tag(name = "Report Intelligence", description = "Endpoints da Laudo Intelligence Layer para estruturar e explicar laudos de forma educacional")
public class ReportIntelligenceController {

    private final ReportIntelligenceService reportIntelligenceService;

    public ReportIntelligenceController(ReportIntelligenceService reportIntelligenceService) {
        this.reportIntelligenceService = reportIntelligenceService;
    }

    @PostMapping("/analyze")
    @Operation(
            summary = "Analisar laudo educacionalmente",
            description = "Recebe texto de laudo, estrutura achados, gera explicacao educacional segura e verifica compatibilidade com o modelo WDBC. Nao realiza diagnostico medico."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Laudo analisado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReportAnalysisResponse.class),
                            examples = @ExampleObject(
                                    name = "report-analysis-created",
                                    value = """
                                            {
                                              "id": 1,
                                              "created_at": "2026-05-30T12:00:00-03:00",
                                              "status": "COMPLETED",
                                              "inputType": "TEXT",
                                              "detectedLanguage": "pt-BR",
                                              "targetLanguage": "pt-BR",
                                              "reportType": "MAMMOGRAPHY",
                                              "structuredFindings": {
                                                "birads": "BI-RADS 3",
                                                "breastSide": "left",
                                                "location": "upper outer quadrant",
                                                "measurements": [],
                                                "mentionedFindings": ["nodule mentioned"],
                                                "mentionedRecommendations": []
                                              },
                                              "importantTerms": [],
                                              "educationalSummary": "Resumo educacional seguro.",
                                              "simpleExplanation": "Explicacao em linguagem simples.",
                                              "wdbcCompatibility": {
                                                "canRunPrediction": false,
                                                "reason": "This report can be explained educationally, but it does not contain the 30 numerical features required by the current WDBC model.",
                                                "detectedFeaturesCount": 0,
                                                "missingFeaturesCount": 30,
                                                "requiredFeaturesCount": 30,
                                                "detectedFeatureNames": []
                                              },
                                              "safetyNotes": [
                                                "Educational only — BreastCare AI does not provide medical diagnosis."
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ReportAnalysisResponse> analyze(
            @org.springframework.web.bind.annotation.RequestBody @Valid AnalyzeReportRequest request
    ) {
        return ResponseEntity.ok(reportIntelligenceService.analyze(request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar detalhe de uma analise de laudo",
            description = "Retorna o detalhe completo de uma analise criada pela Laudo Intelligence Layer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Analise de laudo encontrada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReportAnalysisResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Analise de laudo nao encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ReportAnalysisResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reportIntelligenceService.findById(id));
    }

    @GetMapping
    @Operation(
            summary = "Listar analises de laudo",
            description = "Retorna o historico das analises criadas pela Laudo Intelligence Layer."
    )
    public ResponseEntity<List<ReportAnalysisResponse>> findAll() {
        return ResponseEntity.ok(reportIntelligenceService.findAll());
    }
}
