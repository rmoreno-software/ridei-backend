package com.ridei.identity.application.port.in;

import com.ridei.identity.application.EmailAvailabilityResult;

public interface CheckEmailAvailabilityUseCase {
    EmailAvailabilityResult check(String rawEmail);
}
