package com.evfinder.exception;

import com.evfinder.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Campos do corpo que não passaram nas validações dos DTOs (@NotBlank, @NotNull...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String invalidFields = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, "Campos inválidos: " + invalidFields);
    }

    // Parâmetro com tipo errado (ex: lat=abc) ou JSON malformado no corpo da requisição
    @ExceptionHandler({TypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Requisição inválida: verifique os parâmetros e o corpo enviados.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        // Erros do próprio Spring (parâmetro obrigatório faltando, rota inexistente, método não suportado...)
        // já trazem o status HTTP correto, então mantemos esse status em vez de devolver 500
        if (ex instanceof ErrorResponse errorResponse) {
            return buildResponse(errorResponse.getStatusCode(), errorResponse.getBody().getDetail());
        }

        log.error("Erro inesperado ao processar a requisição", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado no servidor.");
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatusCode status, String message) {
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                httpStatus != null ? httpStatus.getReasonPhrase() : "Error",
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(response);
    }
}
