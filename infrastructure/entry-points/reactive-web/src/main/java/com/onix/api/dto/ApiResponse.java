package com.onix.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int httpCode,
        String httpMessage,
        String timestamp,
        String userMessage,
        T data
) {
    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(code, message, Instant.now().toString(), null, data);
    }

    public static <T> ApiResponse<T> error(int code, String message, String userFriendlyError) {
        return new ApiResponse<>(code, message, Instant.now().toString(), userFriendlyError, null);
    }
}
