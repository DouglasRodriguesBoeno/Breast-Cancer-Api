package brest_cancer.api.report_intelligence.persistence.entity;

import brest_cancer.api.report_intelligence.dto.ImportantTerm;
import brest_cancer.api.report_intelligence.dto.StructuredFindings;
import brest_cancer.api.report_intelligence.dto.WdbcCompatibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "report_analysis_record")
public class ReportAnalysisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "input_type", nullable = false, length = 30)
    private String inputType;

    @Column(name = "detected_language", length = 20)
    private String detectedLanguage;

    @Column(name = "target_language", nullable = false, length = 20)
    private String targetLanguage;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_findings", nullable = false, columnDefinition = "jsonb")
    private StructuredFindings structuredFindings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "important_terms", nullable = false, columnDefinition = "jsonb")
    private List<ImportantTerm> importantTerms;

    @Column(name = "educational_summary", nullable = false, columnDefinition = "TEXT")
    private String educationalSummary;

    @Column(name = "simple_explanation", nullable = false, columnDefinition = "TEXT")
    private String simpleExplanation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "wdbc_compatibility", nullable = false, columnDefinition = "jsonb")
    private WdbcCompatibility wdbcCompatibility;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "safety_notes", nullable = false, columnDefinition = "jsonb")
    private List<String> safetyNotes;

    @Column(name = "processing_status", nullable = false, length = 30)
    private String processingStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "provider_model", length = 100)
    private String providerModel;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}
