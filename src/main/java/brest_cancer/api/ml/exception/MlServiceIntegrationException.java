package brest_cancer.api.ml.exception;

public class MlServiceIntegrationException extends RuntimeException {

    public MlServiceIntegrationException(String message) {
        super(message);
    }

    public MlServiceIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
