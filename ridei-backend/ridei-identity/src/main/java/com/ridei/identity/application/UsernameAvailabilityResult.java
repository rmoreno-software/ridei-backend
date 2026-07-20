package com.ridei.identity.application;

import com.ridei.identity.domain.model.UsernameAvailabilityReason;

public record UsernameAvailabilityResult(
    boolean available,
    UsernameAvailabilityReason reason
) {
    public static final UsernameAvailabilityResult AVAILABLE =
        new UsernameAvailabilityResult(true, UsernameAvailabilityReason.AVAILABLE);

    public static UsernameAvailabilityResult unavailable(UsernameAvailabilityReason reason) {
        return new UsernameAvailabilityResult(false, reason);
    }
}
