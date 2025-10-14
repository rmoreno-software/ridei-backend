package com.ridei.apirest.ridei_apirest.common.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final LocalDateTime timeStamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(null)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .build();
    }
}
