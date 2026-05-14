package com.applyflow.ai.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException exception) {
        Map<String, Object> body = baseBody(exception.getErrorCode(), exception.getMessage());
        return ResponseEntity.status(exception.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, Object> body = baseBody("VALIDATION_ERROR", "Validation failed");
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::fieldError)
                .collect(Collectors.toList());
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception exception) {
        Map<String, Object> body = baseBody("INTERNAL_SERVER_ERROR", exception.getMessage() == null ? "Unexpected error" : exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private Map<String, Object> baseBody(String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("code", code);
        body.put("message", message);
        return body;
    }

    private Map<String, String> fieldError(FieldError error) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("field", error.getField());
        map.put("message", error.getDefaultMessage());
        return map;
    }
}
