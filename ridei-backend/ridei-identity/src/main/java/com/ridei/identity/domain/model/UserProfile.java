package com.ridei.identity.domain.model;

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
    String racingLicenseNumber
) {
    public boolean isSuspended() {
        return status == AccountStatus.SUSPENDED;
    }
}
