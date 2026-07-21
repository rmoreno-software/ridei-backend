package com.ridei.identity.application;

import com.ridei.identity.domain.model.Gender;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.Username;

public record SaveOnboardingStep1Command(
    UserId userId,
    String firstName,
    String lastName,
    Username username,
    Gender gender
) {}
