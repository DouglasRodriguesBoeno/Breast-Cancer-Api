package brest_cancer.api.prediction.persistence.repository;

import brest_cancer.api.prediction.persistence.entity.PredictionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionRecordRepository extends JpaRepository<PredictionRecord, Long> {

    List<PredictionRecord> findAllByOrderByCreatedAtDesc();
}