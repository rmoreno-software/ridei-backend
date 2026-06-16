package com.ridei.identity.infrastructure.adapter.in.rest.exception;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

@Getter
public class ApiError {

    private final int status;
    private final String error;
    private final String message;
    private final List<String> details;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;

    public ApiError(
        int status,
        String error,
        String message,
        List<String> details
    ) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
        this.timestamp = Instant.now();
    }

    public ApiError(int status, String error, String message) {
        this(status, error, message, List.of());
    }

}
