package brest_cancer.api.report_intelligence.service;

import brest_cancer.api.report_intelligence.dto.AnalyzeReportRequest;
import brest_cancer.api.report_intelligence.dto.ReportAnalysisResponse;
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

@Service
public class ReportIntelligenceService {

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
        ReportIntelligenceResult result = reportIntelligenceProvider.analyze(request);

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

    private String normalizeInputType(String inputType) {
        String normalized = inputType == null ? "TEXT" : inputType.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("TEXT") && !normalized.equals("PDF")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inputType deve ser TEXT ou PDF");
        }
        return normalized;
    }
}
