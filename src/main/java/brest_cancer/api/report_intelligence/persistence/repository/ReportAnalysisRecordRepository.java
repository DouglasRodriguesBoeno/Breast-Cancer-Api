package brest_cancer.api.report_intelligence.persistence.repository;

import brest_cancer.api.report_intelligence.persistence.entity.ReportAnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportAnalysisRecordRepository extends JpaRepository<ReportAnalysisRecord, Long> {

    List<ReportAnalysisRecord> findAllByOrderByCreatedAtDesc();
}
