package com.ridei.identity.infrastructure.adapter.in.rest;

import com.ridei.identity.application.EmailAvailabilityResult;

public record EmailAvailabilityResponseDTO(
    boolean available,
    String reason
) {
    public static EmailAvailabilityResponseDTO fromResult(EmailAvailabilityResult result) {
        return new EmailAvailabilityResponseDTO(result.available(), result.reason().name());
    }
}
