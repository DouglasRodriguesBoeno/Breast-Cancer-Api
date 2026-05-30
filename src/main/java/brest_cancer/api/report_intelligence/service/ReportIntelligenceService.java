package brest_cancer.api.report_intelligence.service;

import brest_cancer.api.report_intelligence.dto.AnalyzeReportRequest;
import brest_cancer.api.report_intelligence.dto.ImportantTerm;
import brest_cancer.api.report_intelligence.dto.ReportAnalysisResponse;
import brest_cancer.api.report_intelligence.dto.StructuredFindings;
import brest_cancer.api.report_intelligence.persistence.entity.ReportAnalysisRecord;
import brest_cancer.api.report_intelligence.persistence.repository.ReportAnalysisRecordRepository;
import brest_cancer.api.report_intelligence.provider.ReportIntelligenceProvider;
import brest_cancer.api.report_intelligence.provider.ReportIntelligenceResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportIntelligenceService {

    private static final Pattern BIRADS_VALUE_PATTERN = Pattern.compile("(?i)^(?:BI[-\\s]?RADS\\s*)?([0-6](?:[ABC])?)$");

    private final ReportAnalysisRecordRepository reportAnalysisRecordRepository;
    private final ReportIntelligenceProvider reportIntelligenceProvider;

    public ReportIntelligenceService(
            ReportAnalysisRecordRepository reportAnalysisRecordRepository,
            ReportIntelligenceProvider reportIntelligenceProvider
    ) {
        this.reportAnalysisRecordRepository = reportAnalysisRecordRepository;
        this.reportIntelligenceProvider = reportIntelligenceProvider;
    }

    @Transactional
    public ReportAnalysisResponse analyze(AnalyzeReportRequest request) {
        ReportIntelligenceResult rawResult = reportIntelligenceProvider.analyze(request);
        ReportIntelligenceResult result = normalizeResult(rawResult);

        ReportAnalysisRecord record = new ReportAnalysisRecord();
        record.setInputType(normalizeInputType(request.inputType()));
        record.setDetectedLanguage(result.detectedLanguage());
        record.setTargetLanguage(request.targetLanguage());
        record.setReportType(result.reportType());
        record.setRawText(Boolean.TRUE.equals(request.persistRawText()) ? request.reportText().trim() : null);
        record.setStructuredFindings(result.structuredFindings());
        record.setImportantTerms(result.importantTerms());
        record.setEducationalSummary(result.educationalSummary());
        record.setSimpleExplanation(result.simpleExplanation());
        record.setWdbcCompatibility(result.wdbcCompatibility());
        record.setSafetyNotes(result.safetyNotes());
        record.setProcessingStatus("COMPLETED");
        record.setProvider(result.provider());
        record.setProviderModel(result.providerModel());

        ReportAnalysisRecord saved = reportAnalysisRecordRepository.saveAndFlush(record);
        return ReportAnalysisResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ReportAnalysisResponse findById(Long id) {
        ReportAnalysisRecord record = reportAnalysisRecordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Analise de laudo nao encontrada para o id: " + id
                ));

        return ReportAnalysisResponse.from(record);
    }

    @Transactional(readOnly = true)
    public List<ReportAnalysisResponse> findAll() {
        return reportAnalysisRecordRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ReportAnalysisResponse::from)
                .toList();
    }

    private ReportIntelligenceResult normalizeResult(ReportIntelligenceResult result) {
        StructuredFindings normalizedFindings = normalizeStructuredFindings(result.structuredFindings());
        List<ImportantTerm> normalizedTerms = normalizeImportantTerms(result.importantTerms());

        return new ReportIntelligenceResult(
                result.detectedLanguage(),
                result.reportType(),
                normalizedFindings,
                normalizedTerms,
                sanitizeEducationalText(result.educationalSummary()),
                sanitizeEducationalText(result.simpleExplanation()),
                result.wdbcCompatibility(),
                normalizeSafetyNotes(result.safetyNotes()),
                result.provider(),
                result.providerModel()
        );
    }

    private StructuredFindings normalizeStructuredFindings(StructuredFindings findings) {
        if (findings == null) {
            return null;
        }

        return new StructuredFindings(
                normalizeBirads(findings.birads()),
                findings.breastSide(),
                findings.location(),
                findings.measurements(),
                findings.mentionedFindings(),
                findings.mentionedRecommendations()
        );
    }

    private String normalizeBirads(String birads) {
        if (birads == null || birads.isBlank()) {
            return birads;
        }

        Matcher matcher = BIRADS_VALUE_PATTERN.matcher(birads.trim());
        if (matcher.matches()) {
            return "BI-RADS " + matcher.group(1).toUpperCase(Locale.ROOT);
        }

        return birads.trim();
    }

    private List<ImportantTerm> normalizeImportantTerms(List<ImportantTerm> terms) {
        if (terms == null) {
            return List.of();
        }

        return terms.stream()
                .map(term -> new ImportantTerm(
                        normalizeBirads(term.term()),
                        sanitizeEducationalText(term.explanation())
                ))
                .toList();
    }

    private List<String> normalizeSafetyNotes(List<String> safetyNotes) {
        if (safetyNotes == null || safetyNotes.isEmpty()) {
            return List.of(
                    "Conteudo educacional: nao substitui avaliacao de profissional de saude.",
                    "O sistema nao fornece diagnostico, tratamento ou definicao de urgencia clinica.",
                    "A interpretacao completa deve ser feita com um profissional qualificado."
            );
        }

        return safetyNotes.stream()
                .map(this::sanitizeEducationalText)
                .toList();
    }

    private String sanitizeEducationalText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        return text
                .replace("provavelmente não é perigoso", "é descrito como provavelmente benigno, mas requer acompanhamento conforme orientação médica")
                .replace("provavelmente nao e perigoso", "é descrito como provavelmente benigno, mas requer acompanhamento conforme orientação médica")
                .replace("provavelmente não perigoso", "descrito como provavelmente benigno, mas requer acompanhamento conforme orientação médica")
                .replace("provavelmente nao perigoso", "descrito como provavelmente benigno, mas requer acompanhamento conforme orientação médica")
                .replace("não é perigoso", "deve ser interpretado no contexto clínico por um profissional de saúde")
                .replace("nao e perigoso", "deve ser interpretado no contexto clínico por um profissional de saúde")
                .replace("não perigoso", "deve ser interpretado no contexto clínico por um profissional de saúde")
                .replace("nao perigoso", "deve ser interpretado no contexto clínico por um profissional de saúde")
                .replace("menores chances de malignidade", "características que podem estar associadas a menor suspeição, dependendo do contexto do exame")
                .replace("menor chance de malignidade", "características que podem estar associadas a menor suspeição, dependendo do contexto do exame")
                .trim();
    }

    private String normalizeInputType(String inputType) {
        String normalized = inputType == null ? "TEXT" : inputType.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("TEXT") && !normalized.equals("PDF")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inputType deve ser TEXT ou PDF");
        }
        return normalized;
    }
}
