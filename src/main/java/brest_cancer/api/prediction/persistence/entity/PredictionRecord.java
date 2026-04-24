package brest_cancer.api.prediction.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "prediction_record")
public class PredictionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_features", nullable = false, columnDefinition = "jsonb")
    private Map<String, Double> inputFeatures;

    @Column(name = "predicted_label", nullable = false, length = 20)
    private String predictedLabel;

    @Column(name = "predicted_label_name", nullable = false, length = 50)
    private String predictedLabelName;

    @Column(name = "probability_malignant", nullable = false)
    private double probabilityMalignant;

    @Column(name = "probability_benign", nullable = false)
    private double probabilityBenign;

    @Column(name = "used_threshold_malignant", nullable = false)
    private double usedThresholdMalignant;

    @Column(name = "model_type", nullable = false, length = 100)
    private String modelType;

    @Column(name = "risk_band", nullable = false, length = 30)
    private String riskBand;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "confidence_note", columnDefinition = "TEXT")
    private String confidenceNote;

    @Column(name = "input_quality_note", columnDefinition = "TEXT")
    private String inputQualityNote;

    @Column(name = "clinical_note", columnDefinition = "TEXT")
    private String clinicalNote;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "used_features", nullable = false, columnDefinition = "jsonb")
    private List<String> usedFeatures;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ignored_features", nullable = false, columnDefinition = "jsonb")
    private List<String> ignoredFeatures;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "imputed_features", nullable = false, columnDefinition = "jsonb")
    private List<String> imputedFeatures;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warnings", nullable = false, columnDefinition = "jsonb")
    private List<String> warnings;
}