package brest_cancer.api.report_intelligence.service;

import brest_cancer.api.report_intelligence.dto.*;
import brest_cancer.api.report_intelligence.persistence.entity.ReportAnalysisRecord;
import brest_cancer.api.report_intelligence.persistence.repository.ReportAnalysisRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportIntelligenceService {

    private static final int REQUIRED_WDBC_FEATURES_COUNT = 30;

    private static final Pattern BIRADS_PATTERN = Pattern.compile(
            "(?i)BI[-\\s]?RADS\\s*[:\\-]?\\s*([0-6])"
    );

    private static final Pattern MEASUREMENT_PATTERN = Pattern.compile(
            "(?i)(\\d+(?:[\\.,]\\d+)?)\\s*(mm|cm)"
    );

    private static final List<String> WDBC_FEATURE_NAMES = List.of(
            "radius_mean", "texture_mean", "perimeter_mean", "area_mean", "smoothness_mean",
            "compactness_mean", "concavity_mean", "concave_points_mean", "symmetry_mean", "fractal_dimension_mean",
            "radius_se", "texture_se", "perimeter_se", "area_se", "smoothness_se",
            "compactness_se", "concavity_se", "concave_points_se", "symmetry_se", "fractal_dimension_se",
            "radius_worst", "texture_worst", "perimeter_worst", "area_worst", "smoothness_worst",
            "compactness_worst", "concavity_worst", "concave_points_worst", "symmetry_worst", "fractal_dimension_worst"
    );

    private final ReportAnalysisRecordRepository reportAnalysisRecordRepository;

    public ReportIntelligenceService(ReportAnalysisRecordRepository reportAnalysisRecordRepository) {
        this.reportAnalysisRecordRepository = reportAnalysisRecordRepository;
    }

    @Transactional
    public ReportAnalysisResponse analyze(AnalyzeReportRequest request) {
        String reportText = request.reportText().trim();
        String normalizedText = reportText.toLowerCase(Locale.ROOT);

        StructuredFindings structuredFindings = extractStructuredFindings(reportText, normalizedText);
        List<ImportantTerm> importantTerms = extractImportantTerms(normalizedText);
        WdbcCompatibility wdbcCompatibility = checkWdbcCompatibility(normalizedText);
        String detectedLanguage = detectLanguage(normalizedText);
        String reportType = normalizeReportType(request.reportType(), normalizedText);

        ReportAnalysisRecord record = new ReportAnalysisRecord();
        record.setInputType(normalizeInputType(request.inputType()));
        record.setDetectedLanguage(detectedLanguage);
        record.setTargetLanguage(request.targetLanguage());
        record.setReportType(reportType);
        record.setRawText(Boolean.TRUE.equals(request.persistRawText()) ? reportText : null);
        record.setStructuredFindings(structuredFindings);
        record.setImportantTerms(importantTerms);
        record.setEducationalSummary(buildEducationalSummary(structuredFindings, reportType));
        record.setSimpleExplanation(buildSimpleExplanation(structuredFindings));
        record.setWdbcCompatibility(wdbcCompatibility);
        record.setSafetyNotes(buildSafetyNotes());
        record.setProcessingStatus("COMPLETED");
        record.setProvider("RULE_BASED");
        record.setProviderModel("report-intelligence-rule-based-v1");

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

    private StructuredFindings extractStructuredFindings(String reportText, String normalizedText) {
        String birads = extractBirads(reportText);
        String breastSide = extractBreastSide(normalizedText);
        String location = extractLocation(normalizedText);
        List<ReportMeasurement> measurements = extractMeasurements(reportText);
        List<String> mentionedFindings = extractMentionedFindings(normalizedText);
        List<String> mentionedRecommendations = extractMentionedRecommendations(normalizedText);

        return new StructuredFindings(
                birads,
                breastSide,
                location,
                measurements,
                mentionedFindings,
                mentionedRecommendations
        );
    }

    private String extractBirads(String reportText) {
        Matcher matcher = BIRADS_PATTERN.matcher(reportText);
        if (matcher.find()) {
            return "BI-RADS " + matcher.group(1);
        }
        return null;
    }

    private String extractBreastSide(String normalizedText) {
        if (containsAny(normalizedText, "mama esquerda", "left breast", "esquerda")) {
            return "left";
        }
        if (containsAny(normalizedText, "mama direita", "right breast", "direita")) {
            return "right";
        }
        if (containsAny(normalizedText, "bilateral", "ambas as mamas", "both breasts")) {
            return "bilateral";
        }
        return null;
    }

    private String extractLocation(String normalizedText) {
        if (containsAny(normalizedText, "quadrante superior externo", "upper outer quadrant")) {
            return "upper outer quadrant";
        }
        if (containsAny(normalizedText, "quadrante superior interno", "upper inner quadrant")) {
            return "upper inner quadrant";
        }
        if (containsAny(normalizedText, "quadrante inferior externo", "lower outer quadrant")) {
            return "lower outer quadrant";
        }
        if (containsAny(normalizedText, "quadrante inferior interno", "lower inner quadrant")) {
            return "lower inner quadrant";
        }
        return null;
    }

    private List<ReportMeasurement> extractMeasurements(String reportText) {
        List<ReportMeasurement> measurements = new ArrayList<>();
        Matcher matcher = MEASUREMENT_PATTERN.matcher(reportText);

        while (matcher.find()) {
            String rawValue = matcher.group(1).replace(',', '.');
            double value = Double.parseDouble(rawValue);
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            measurements.add(new ReportMeasurement(
                    value,
                    unit,
                    "Measurement mentioned in the provided report text."
            ));
        }

        return measurements;
    }

    private List<String> extractMentionedFindings(String normalizedText) {
        List<String> findings = new ArrayList<>();

        addIfPresent(findings, normalizedText, "nodule mentioned", "nodulo", "nódulo", "nodule");
        addIfPresent(findings, normalizedText, "calcifications mentioned", "calcificacao", "calcificação", "calcifications");
        addIfPresent(findings, normalizedText, "asymmetry mentioned", "assimetria", "asymmetry");
        addIfPresent(findings, normalizedText, "circumscribed margins mentioned", "margens circunscritas", "circumscribed margins");
        addIfPresent(findings, normalizedText, "irregular margins mentioned", "margens irregulares", "irregular margins");

        return findings;
    }

    private List<String> extractMentionedRecommendations(String normalizedText) {
        List<String> recommendations = new ArrayList<>();

        addIfPresent(recommendations, normalizedText, "follow-up mentioned in the report", "controle", "acompanhamento", "follow-up");
        addIfPresent(recommendations, normalizedText, "biopsy mentioned in the report", "biopsia", "biopsy");
        addIfPresent(recommendations, normalizedText, "additional imaging mentioned in the report", "complementacao", "complementação", "additional imaging");

        return recommendations;
    }

    private List<ImportantTerm> extractImportantTerms(String normalizedText) {
        List<ImportantTerm> terms = new ArrayList<>();

        if (containsAny(normalizedText, "bi-rads", "birads")) {
            terms.add(new ImportantTerm(
                    "BI-RADS",
                    "A standardized breast imaging reporting category mentioned in many mammography, ultrasound or MRI reports."
            ));
        }
        if (containsAny(normalizedText, "nodulo", "nódulo", "nodule")) {
            terms.add(new ImportantTerm(
                    "Nodule",
                    "A localized finding described in breast tissue. Its meaning depends on imaging characteristics and professional evaluation."
            ));
        }
        if (containsAny(normalizedText, "calcificacao", "calcificação", "calcifications")) {
            terms.add(new ImportantTerm(
                    "Calcifications",
                    "Small calcium deposits that may be described in breast imaging reports. Their relevance depends on pattern and context."
            ));
        }
        if (containsAny(normalizedText, "margens", "margins")) {
            terms.add(new ImportantTerm(
                    "Margins",
                    "A term used to describe the borders of a finding in the provided report text."
            ));
        }

        return terms;
    }

    private WdbcCompatibility checkWdbcCompatibility(String normalizedText) {
        List<String> detectedFeatures = WDBC_FEATURE_NAMES.stream()
                .filter(normalizedText::contains)
                .toList();

        boolean canRunPrediction = detectedFeatures.size() == REQUIRED_WDBC_FEATURES_COUNT;
        int missingFeaturesCount = REQUIRED_WDBC_FEATURES_COUNT - detectedFeatures.size();

        String reason = canRunPrediction
                ? "Structured numerical feature names compatible with the WDBC model were detected."
                : "This report can be explained educationally, but it does not contain the 30 numerical features required by the current WDBC model.";

        return new WdbcCompatibility(
                canRunPrediction,
                reason,
                detectedFeatures.size(),
                missingFeaturesCount,
                REQUIRED_WDBC_FEATURES_COUNT,
                detectedFeatures
        );
    }

    private String buildEducationalSummary(StructuredFindings findings, String reportType) {
        String biradsText = findings.birads() != null
                ? " The provided text mentions " + findings.birads() + "."
                : " No BI-RADS category was identified in the provided text.";

        return "BreastCare AI structured the provided " + reportType.toLowerCase(Locale.ROOT)
                + " report text into educational findings." + biradsText
                + " This summary is educational and should be reviewed with a healthcare professional.";
    }

    private String buildSimpleExplanation(StructuredFindings findings) {
        if (findings.birads() != null) {
            return "In simple language, the report text mentions " + findings.birads()
                    + ". BreastCare AI explains the provided information for educational purposes only and does not provide diagnosis.";
        }

        return "In simple language, BreastCare AI identified terms and findings mentioned in the report text. This explanation is educational only and does not replace professional medical evaluation.";
    }

    private List<String> buildSafetyNotes() {
        return List.of(
                "Educational only — BreastCare AI does not provide medical diagnosis.",
                "This explanation does not replace professional medical evaluation.",
                "The system does not recommend treatment or define clinical urgency.",
                "The WDBC model only runs when compatible structured numerical features are available."
        );
    }

    private String detectLanguage(String normalizedText) {
        if (containsAny(normalizedText, " laudo ", " mama ", " nódulo ", " nodulo ", " exame ")) {
            return "pt-BR";
        }
        if (containsAny(normalizedText, " informe ", " mama ", " nódulo ", " examen ")) {
            return "es";
        }
        return "en";
    }

    private String normalizeInputType(String inputType) {
        String normalized = inputType == null ? "TEXT" : inputType.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("TEXT") && !normalized.equals("PDF")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inputType deve ser TEXT ou PDF");
        }
        return normalized;
    }

    private String normalizeReportType(String requestedReportType, String normalizedText) {
        if (requestedReportType != null && !requestedReportType.isBlank()) {
            return requestedReportType.trim().toUpperCase(Locale.ROOT);
        }
        if (containsAny(normalizedText, "mamografia", "mammography")) {
            return "MAMMOGRAPHY";
        }
        if (containsAny(normalizedText, "ultrassom", "ultrasonografia", "ultrasound")) {
            return "ULTRASOUND";
        }
        if (containsAny(normalizedText, "ressonancia", "ressonância", "mri")) {
            return "MRI";
        }
        if (containsAny(normalizedText, "biopsia", "biopsy")) {
            return "BIOPSY";
        }
        return "UNKNOWN";
    }

    private void addIfPresent(List<String> values, String text, String value, String... terms) {
        if (containsAny(text, terms)) {
            values.add(value);
        }
    }

    private boolean containsAny(String text, String... terms) {
        return Arrays.stream(terms).anyMatch(text::contains);
    }
}
