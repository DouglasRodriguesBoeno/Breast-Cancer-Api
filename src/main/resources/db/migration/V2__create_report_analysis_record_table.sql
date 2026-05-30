CREATE TABLE report_analysis_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    input_type VARCHAR(30) NOT NULL,
    detected_language VARCHAR(20),
    target_language VARCHAR(20) NOT NULL,
    report_type VARCHAR(50) NOT NULL,

    raw_text TEXT,

    structured_findings JSONB NOT NULL,
    important_terms JSONB NOT NULL,

    educational_summary TEXT NOT NULL,
    simple_explanation TEXT NOT NULL,

    wdbc_compatibility JSONB NOT NULL,
    safety_notes JSONB NOT NULL,

    processing_status VARCHAR(30) NOT NULL,
    error_message TEXT,

    provider VARCHAR(100),
    provider_model VARCHAR(100),
    correlation_id VARCHAR(100)
);

CREATE INDEX idx_report_analysis_record_created_at
    ON report_analysis_record (created_at DESC);

CREATE INDEX idx_report_analysis_record_report_type
    ON report_analysis_record (report_type);

CREATE INDEX idx_report_analysis_record_target_language
    ON report_analysis_record (target_language);
