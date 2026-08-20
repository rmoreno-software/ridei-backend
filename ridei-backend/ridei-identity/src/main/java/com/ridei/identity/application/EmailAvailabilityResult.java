package com.ridei.identity.application;

import com.ridei.identity.domain.model.EmailAvailabilityReason;

public record EmailAvailabilityResult(
    boolean available,
    EmailAvailabilityReason reason
) {
    public static final EmailAvailabilityResult AVAILABLE =
        new EmailAvailabilityResult(true, EmailAvailabilityReason.AVAILABLE);

    public static EmailAvailabilityResult unavailable(EmailAvailabilityReason reason) {
        return new EmailAvailabilityResult(false, reason);
    }
}
