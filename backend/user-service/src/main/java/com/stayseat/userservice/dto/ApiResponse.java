package com.stayseat.userservice.dto;

public record ApiResponse<T>(boolean success, T data, Object meta) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, data, null);
    }
}
