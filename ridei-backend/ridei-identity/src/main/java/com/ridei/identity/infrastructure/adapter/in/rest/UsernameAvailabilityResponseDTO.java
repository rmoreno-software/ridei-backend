package com.ridei.identity.infrastructure.adapter.in.rest;

import com.ridei.identity.application.UsernameAvailabilityResult;

public record UsernameAvailabilityResponseDTO(
    boolean available,
    String reason
) {
    public static UsernameAvailabilityResponseDTO fromResult(UsernameAvailabilityResult result) {
        return new UsernameAvailabilityResponseDTO(result.available(), result.reason().name());
    }
}
