package com.stayseat.restaurantbooking.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private record ErrorBody(String code, String message, Object details) {}
    private record ErrorEnvelope(boolean success, ErrorBody error) {}

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorEnvelope> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorEnvelope(false, new ErrorBody(ex.getCode(), ex.getMessage(), null)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEnvelope> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorEnvelope(false,
                        new ErrorBody("VALIDATION_ERROR", "One or more fields are invalid.", fieldErrors)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorEnvelope> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorEnvelope(false,
                        new ErrorBody("VALIDATION_ERROR", ex.getMessage(), null)));
    }

    /**
     * Thrown when the Postgres partial unique index (see V1__init.sql) rejects
     * an insert because another PENDING/CONFIRMED booking already holds this
     * table for the same date + time slot. This is the safety net that catches
     * the race the application-layer availability check alone can't fully
     * prevent.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorEnvelope> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorEnvelope(false, new ErrorBody(
                        "TABLE_NOT_AVAILABLE",
                        "The selected table is not available for the given date and time slot.",
                        null)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorEnvelope> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorEnvelope(false,
                        new ErrorBody("INTERNAL_ERROR", "Something went wrong. Please try again.", null)));
    }
}
