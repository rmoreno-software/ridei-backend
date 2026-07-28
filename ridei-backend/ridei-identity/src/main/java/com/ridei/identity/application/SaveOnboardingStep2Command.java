package com.ridei.identity.application;

import java.time.LocalDate;

import com.ridei.identity.domain.model.PhoneNumber;
import com.ridei.identity.domain.model.UserId;

public record SaveOnboardingStep2Command(
    UserId userId,
    LocalDate dateOfBirth,
    String countryCode,
    PhoneNumber phoneNumber
) {}
