package brest_cancer.api.prediction.service;

import brest_cancer.api.ml.client.MlServiceClient;
import brest_cancer.api.ml.dto.MlPredictResponse;
import brest_cancer.api.ml.exception.MlServiceIntegrationException;
import brest_cancer.api.prediction.dto.PredictionListItemResponse;
import brest_cancer.api.prediction.dto.PredictionRequest;
import brest_cancer.api.prediction.dto.PredictionResponse;
import brest_cancer.api.prediction.persistence.entity.PredictionRecord;
import brest_cancer.api.prediction.persistence.repository.PredictionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private MlServiceClient mlServiceClient;

    @Mock
    private PredictionRecordRepository predictionRecordRepository;

    @InjectMocks
    private PredictionService predictionService;

    private PredictionRequest predictionRequest;
    private MlPredictResponse mlPredictResponse;

    @BeforeEach
    void setUp() {
        predictionRequest = new PredictionRequest(Map.of(
                "radius_mean", 17.99,
                "texture_mean", 10.38
        ));

        mlPredictResponse = new MlPredictResponse(
                "M",
                "Maligno",
                0.9733,
                0.0267,
                0.3433,
                "ensemble_mean_logistic_random_forest",
                "high",
                "Resumo de teste",
                "Confianca alta",
                "Input completo",
                "Nota clinica educacional",
                List.of("radius_mean", "texture_mean"),
                List.of(),
                List.of(),
                List.of("Modelo treinado para fins educacionais.")
        );
    }

    @Test
    void shouldCreatePredictionAndPersistRecord() {
        PredictionRecord savedRecord = new PredictionRecord();
        savedRecord.setInputFeatures(predictionRequest.features());
        savedRecord.setPredictedLabel(mlPredictResponse.predictedLabel());
        savedRecord.setPredictedLabelName(mlPredictResponse.predictedLabelName());
        savedRecord.setProbabilityMalignant(mlPredictResponse.probabilityMalignant());
        savedRecord.setProbabilityBenign(mlPredictResponse.probabilityBenign());
        savedRecord.setUsedThresholdMalignant(mlPredictResponse.usedThresholdMalignant());
        savedRecord.setModelType(mlPredictResponse.modelType());
        savedRecord.setRiskBand(mlPredictResponse.riskBand());
        savedRecord.setSummary(mlPredictResponse.summary());
        savedRecord.setConfidenceNote(mlPredictResponse.confidenceNote());
        savedRecord.setInputQualityNote(mlPredictResponse.inputQualityNote());
        savedRecord.setClinicalNote(mlPredictResponse.clinicalNote());
        savedRecord.setUsedFeatures(mlPredictResponse.usedFeatures());
        savedRecord.setIgnoredFeatures(mlPredictResponse.ignoredFeatures());
        savedRecord.setImputedFeatures(mlPredictResponse.imputedFeatures());
        savedRecord.setWarnings(mlPredictResponse.warnings());

        var idField = getClassField("id");
        var createdAtField = getClassField("createdAt");
        try {
            idField.set(savedRecord, 1L);
            createdAtField.set(savedRecord, OffsetDateTime.parse("2026-04-29T19:57:53.597746-03:00"));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        when(mlServiceClient.predict(any())).thenReturn(mlPredictResponse);
        when(predictionRecordRepository.saveAndFlush(any(PredictionRecord.class))).thenReturn(savedRecord);

        PredictionResponse response = predictionService.createPrediction(predictionRequest);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("M", response.predictedLabel());
        assertEquals("Maligno", response.predictedLabelName());
        assertEquals(0.9733, response.probabilityMalignant());
        assertEquals("high", response.riskBand());

        ArgumentCaptor<PredictionRecord> captor = ArgumentCaptor.forClass(PredictionRecord.class);
        verify(predictionRecordRepository).saveAndFlush(captor.capture());

        PredictionRecord captured = captor.getValue();
        assertEquals(predictionRequest.features(), captured.getInputFeatures());
        assertEquals("M", captured.getPredictedLabel());
        assertEquals("Maligno", captured.getPredictedLabelName());
        assertEquals("ensemble_mean_logistic_random_forest", captured.getModelType());
    }

    @Test
    void shouldReturnPredictionListOrderedFromRepository() {
        PredictionRecord record1 = new PredictionRecord();
        PredictionRecord record2 = new PredictionRecord();

        setField(record1, "id", 2L);
        setField(record1, "createdAt", OffsetDateTime.parse("2026-04-29T20:00:00-03:00"));
        record1.setPredictedLabel("M");
        record1.setPredictedLabelName("Maligno");
        record1.setProbabilityMalignant(0.91);
        record1.setRiskBand("high");
        record1.setModelType("ensemble_mean_logistic_random_forest");

        setField(record2, "id", 1L);
        setField(record2, "createdAt", OffsetDateTime.parse("2026-04-29T19:00:00-03:00"));
        record2.setPredictedLabel("B");
        record2.setPredictedLabelName("Benigno");
        record2.setProbabilityMalignant(0.10);
        record2.setRiskBand("low");
        record2.setModelType("ensemble_mean_logistic_random_forest");

        when(predictionRecordRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(record1, record2));

        List<PredictionListItemResponse> result = predictionService.findAll();

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).id());
        assertEquals(1L, result.get(1).id());
    }

    @Test
    void shouldReturnPredictionById() {
        PredictionRecord record = new PredictionRecord();
        setField(record, "id", 1L);
        setField(record, "createdAt", OffsetDateTime.parse("2026-04-29T19:57:53.597746-03:00"));
        record.setPredictedLabel("M");
        record.setPredictedLabelName("Maligno");
        record.setProbabilityMalignant(0.9733);
        record.setProbabilityBenign(0.0267);
        record.setUsedThresholdMalignant(0.3433);
        record.setModelType("ensemble_mean_logistic_random_forest");
        record.setRiskBand("high");
        record.setSummary("Resumo");
        record.setConfidenceNote("Confianca");
        record.setInputQualityNote("Input ok");
        record.setClinicalNote("Nota clinica");
        record.setUsedFeatures(List.of("radius_mean"));
        record.setIgnoredFeatures(List.of());
        record.setImputedFeatures(List.of());
        record.setWarnings(List.of("warning"));

        when(predictionRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        PredictionResponse response = predictionService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("M", response.predictedLabel());
    }

    @Test
    void shouldThrowNotFoundWhenPredictionDoesNotExist() {
        when(predictionRecordRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> predictionService.findById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Analise nao encontrada"));
    }

    @Test
    void shouldPropagateMlIntegrationException() {
        when(mlServiceClient.predict(any()))
                .thenThrow(new MlServiceIntegrationException("Erro ao consultar predict do ML Service."));

        assertThrows(
                MlServiceIntegrationException.class,
                () -> predictionService.createPrediction(predictionRequest)
        );

        verify(predictionRecordRepository, never()).saveAndFlush(any());
    }

    private static java.lang.reflect.Field getClassField(String name) {
        try {
            java.lang.reflect.Field field = PredictionRecord.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(PredictionRecord record, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = PredictionRecord.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(record, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}