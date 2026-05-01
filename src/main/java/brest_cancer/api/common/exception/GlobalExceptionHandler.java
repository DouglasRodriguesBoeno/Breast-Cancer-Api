package brest_cancer.api.common.exception;

import brest_cancer.api.common.dto.ApiErrorResponse;
import brest_cancer.api.ml.exception.MlServiceIntegrationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        ApiErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message.isBlank() ? "Dados de entrada invalidos." : message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ApiErrorResponse errorResponse = buildErrorResponse(
                status,
                ex.getReason() != null ? ex.getReason() : "Erro na requisicao.",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MlServiceIntegrationException.class)
    public ResponseEntity<ApiErrorResponse> handleMlServiceIntegrationException(
            MlServiceIntegrationException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                "Falha ao consultar o ML Service. Verifique se o servico de inferencia esta disponivel.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ApiErrorResponse buildErrorResponse(
            HttpStatus status,
            String message,
            String path
    ) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }


    private String formatFieldError(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : "valor invalido";

        return fieldError.getField() + ": " + defaultMessage;
    }
}
