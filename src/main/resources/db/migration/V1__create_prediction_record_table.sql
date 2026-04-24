CREATE TABLE prediction_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    input_features JSONB NOT NULL,

    predicted_label VARCHAR(20) NOT NULL,
    predicted_label_name VARCHAR(50) NOT NULL,
    probability_malignant DOUBLE PRECISION NOT NULL,
    probability_benign DOUBLE PRECISION NOT NULL,
    used_threshold_malignant DOUBLE PRECISION NOT NULL,
    model_type VARCHAR(100) NOT NULL,
    risk_band VARCHAR(30) NOT NULL,

    summary TEXT,
    confidence_note TEXT,
    input_quality_note TEXT,
    clinical_note TEXT,

    used_features JSONB NOT NULL,
    ignored_features JSONB NOT NULL,
    imputed_features JSONB NOT NULL,
    warnings JSONB NOT NULL
);

CREATE INDEX idx_prediction_record_created_at
    ON prediction_record (created_at DESC);