package brest_cancer.api.prediction.controller;

import brest_cancer.api.common.exception.GlobalExceptionHandler;
import brest_cancer.api.ml.exception.MlServiceIntegrationException;
import brest_cancer.api.prediction.dto.PredictionListItemResponse;
import brest_cancer.api.prediction.dto.PredictionResponse;
import brest_cancer.api.prediction.service.PredictionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PredictionController.class)
@Import(GlobalExceptionHandler.class)
class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PredictionService predictionService;

    @Test
    void shouldCreatePrediction() throws Exception {
        PredictionResponse response = new PredictionResponse(
                1L,
                OffsetDateTime.parse("2026-04-29T19:57:53.597746-03:00"),
                "M",
                "Maligno",
                0.9733,
                0.0267,
                0.3433,
                "ensemble_mean_logistic_random_forest",
                "high",
                "Resumo",
                "Confianca",
                "Input ok",
                "Nota clinica",
                List.of("radius_mean"),
                List.of(),
                List.of(),
                List.of("warning")
        );

        when(predictionService.createPrediction(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/predictions")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "features": {
                                    "radius_mean": 17.99,
                                    "texture_mean": 10.38
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.predicted_label").value("M"))
                .andExpect(jsonPath("$.risk_band").value("high"));
    }

    @Test
    void shouldReturnPredictionHistory() throws Exception {
        List<PredictionListItemResponse> response = List.of(
                new PredictionListItemResponse(
                        2L,
                        OffsetDateTime.parse("2026-04-29T20:00:00-03:00"),
                        "M",
                        "Maligno",
                        0.91,
                        "high",
                        "ensemble_mean_logistic_random_forest"
                )
        );

        when(predictionService.findAll()).thenReturn(response);

        mockMvc.perform(get("/api/predictions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].predicted_label").value("M"));
    }

    @Test
    void shouldReturnPredictionById() throws Exception {
        PredictionResponse response = new PredictionResponse(
                1L,
                OffsetDateTime.parse("2026-04-29T19:57:53.597746-03:00"),
                "M",
                "Maligno",
                0.9733,
                0.0267,
                0.3433,
                "ensemble_mean_logistic_random_forest",
                "high",
                "Resumo",
                "Confianca",
                "Input ok",
                "Nota clinica",
                List.of("radius_mean"),
                List.of(),
                List.of(),
                List.of("warning")
        );

        when(predictionService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/predictions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.predicted_label").value("M"));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/predictions")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "features": {}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void shouldReturn404WhenPredictionDoesNotExist() throws Exception {
        when(predictionService.findById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Analise nao encontrada para o id: 999"));

        mockMvc.perform(get("/api/predictions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldReturn502WhenMlServiceIsUnavailable() throws Exception {
        when(predictionService.createPrediction(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new MlServiceIntegrationException("Erro ao consultar predict do ML Service."));

        mockMvc.perform(post("/api/predictions")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "features": {
                                    "radius_mean": 17.99
                                  }
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("Bad Gateway"));
    }
}