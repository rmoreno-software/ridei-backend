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
    LocalDate dateOfBirth,
    String countryCode,
    PhoneNumber phoneNumber,
    String profilePictureUrl,
    IdentityDocument identityDocument
) {
    public boolean isSuspended() {
        return status == AccountStatus.SUSPENDED;
    }
}
