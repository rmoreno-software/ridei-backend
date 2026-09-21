package com.ridei.garage.infrastructure.adapter.in.rest.exception;

import java.util.List;

public record ApiError(
    int status,
    String error,
    String message,
    List<String> details
) {
    public ApiError(
        int status,
        String error,
        String message
    ) {
        this(
            status,
            error,
            message,
            List.of()
        );
    }
}
