package com.senura.authservice.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ApiError {
    private String code;
    private String message;
    private Object details;
}

@Data
@NoArgsConstructor
public class ApiErrorResponse {
    private boolean success = false;
    private ApiError error;

    public ApiErrorResponse(String code, String message) {
        this.error = new ApiError(code, message, null);
    }

    public ApiErrorResponse(String code, String message, Object details) {
        this.error = new ApiError(code, message, details);
    }
}