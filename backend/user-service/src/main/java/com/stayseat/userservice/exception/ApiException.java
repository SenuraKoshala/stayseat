package com.stayseat.userservice.exception;

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

    public static ApiException badRequest(String message) {
        return new ApiException("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }

    public static ApiException forbidden(String message) {
        return new ApiException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public static ApiException unauthenticated(String message) {
        return new ApiException("UNAUTHENTICATED", message, HttpStatus.UNAUTHORIZED);
    }

    public static ApiException imageUploadFailed() {
        return new ApiException("IMAGE_UPLOAD_FAILED", "Could not store the uploaded image.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
