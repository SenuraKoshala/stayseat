package com.stayseat.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public ApiException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // --- Convenience factories for this service's error.code enum ---

    public static ApiException notFound(String what) {
        return new ApiException("NOT_FOUND", what + " not found.", HttpStatus.NOT_FOUND);
    }

    public static ApiException forbidden(String message) {
        return new ApiException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public static ApiException unauthenticated(String message) {
        return new ApiException("UNAUTHENTICATED", message, HttpStatus.UNAUTHORIZED);
    }

    public static ApiException duplicateCharge(String bookingId) {
        return new ApiException(
                "DUPLICATE_CHARGE",
                "Booking " + bookingId + " has already been paid for.",
                HttpStatus.CONFLICT
        );
    }

    public static ApiException invalidWebhookSignature() {
        return new ApiException(
                "WEBHOOK_SIGNATURE_INVALID",
                "The webhook signature could not be verified.",
                HttpStatus.UNAUTHORIZED
        );
    }

    public static ApiException invalidWebhookPayload(String detail) {
        return new ApiException("INVALID_WEBHOOK_PAYLOAD", detail, HttpStatus.BAD_REQUEST);
    }

    public static ApiException invalidTransactionState(String message) {
        return new ApiException("INVALID_TRANSACTION_STATE", message, HttpStatus.CONFLICT);
    }
}
