package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.UsernameAvailabilityResult;

public interface CheckUsernameAvailabilityUseCase {
    UsernameAvailabilityResult check(String rawUsername);
}
