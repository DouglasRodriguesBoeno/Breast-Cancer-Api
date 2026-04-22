package brest_cancer.api.ml.client;

import brest_cancer.api.ml.dto.*;
import brest_cancer.api.ml.exception.MlServiceIntegrationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class MlServiceClient {

    private final RestClient restClient;

    public MlServiceClient(RestClient mlRestClient) {
        this.restClient = mlRestClient;
    }

    public MlHealthResponse getHealth() {
        return get("/v1/health", MlHealthResponse.class, "Erro ao consultar health do ML Service.");
    }

    public MlFeaturesResponse getFeatures() {
        return get("/v1/features", MlFeaturesResponse.class, "Erro ao consultar features do ML Service.");
    }

    public MlModelInfoResponse getModelInfo() {
        return get("/v1/model-info", MlModelInfoResponse.class, "Erro ao consultar model-info do ML Service.");
    }

    public MlPredictResponse predict(MlPredictRequest request) {
        try {
            MlPredictResponse response = restClient.post()
                    .uri("/v1/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MlPredictResponse.class);

            if (response == null) {
                throw new MlServiceIntegrationException("Erro ao consultar predict do ML Service. Corpo vazio.");
            }

            return response;
        } catch (RestClientException ex) {
            throw new MlServiceIntegrationException("Erro ao consultar predict do ML Service.", ex);
        }
    }

    private <T> T get(String uri, Class<T> responseType, String errorMessage) {
        try {
            T response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(responseType);

            if (response == null) {
                throw new MlServiceIntegrationException(errorMessage + " Corpo vazio.");
            }

            return response;
        } catch (RestClientException ex) {
            throw new MlServiceIntegrationException(errorMessage, ex);
        }
    }
}
