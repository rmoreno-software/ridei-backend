package com.ridei.identity.domain.model;

import java.time.LocalDate;

public record UserProfile(
    UserId id,
    Email email,
    Username username,
    String firstName,
    String lastName,
    Gender gender,
    UserRole role,
    AccountStatus status,
    String profileType,
    String racingLicenseNumber,
    LocalDate dateOfBirth,
    String countryCode,
    PhoneNumber phoneNumber,
    String profilePictureUrl
) {
    public boolean isSuspended() {
        return status == AccountStatus.SUSPENDED;
    }
}
