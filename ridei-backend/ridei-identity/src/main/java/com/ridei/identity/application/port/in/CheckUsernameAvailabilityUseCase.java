package com.ridei.identity.application.port.in;

import com.ridei.identity.application.UsernameAvailabilityResult;

public interface CheckUsernameAvailabilityUseCase {
    UsernameAvailabilityResult check(String rawUsername);
}
