package com.stayseat.hotelbooking.exception;

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

    public static ApiException roomNotAvailable() {
        return new ApiException(
                "ROOM_NOT_AVAILABLE",
                "The selected room is not available for the given dates.",
                HttpStatus.CONFLICT
        );
    }

    public static ApiException invalidDateRange() {
        return new ApiException(
                "INVALID_DATE_RANGE",
                "checkOutDate must be after checkInDate.",
                HttpStatus.BAD_REQUEST
        );
    }

    public static ApiException forbidden(String message) {
        return new ApiException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public static ApiException unauthenticated(String message) {
        return new ApiException("UNAUTHENTICATED", message, HttpStatus.UNAUTHORIZED);
    }
}
