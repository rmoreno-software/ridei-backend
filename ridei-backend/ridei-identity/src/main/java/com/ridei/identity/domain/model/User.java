package com.ridei.identity.domain.model;

import java.time.Instant;
import java.time.LocalDate;

import com.ridei.identity.domain.exception.MinimumAgeNotMetException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class User {
    
    private final UserId id;
    private Email email;
    private String passwordHash;
    private Username username;
    private String firstName;
    private String lastName;
    private Gender gender;
    private PhoneNumber phoneNumber;
    private LocalDate dateOfBirth;
    private String countryCode;
    private IdentityDocument identityDocument;
    private String googleId;
    private UserRole role;
    private AccountStatus status;
    private boolean termsAccepted;
    private Instant termsAcceptedAt;
    private Instant createdAt;

    public static User register(
        Email email,
        String passwordHash,
        Username username,
        String firstName,
        String lastName,
        Gender gender,
        PhoneNumber phoneNumber,
        LocalDate dateOfBirth,
        String countryCode,
        IdentityDocument identityDocument,
        UserRole role,
        boolean termsAccepted
    ) {
        if (dateOfBirth.isAfter(LocalDate.now().minusYears(16)))
            throw new MinimumAgeNotMetException();
        Instant now = Instant.now();
        return new User(
            UserId.newId(),
            email,
            passwordHash,
            username,
            firstName,
            lastName,
            gender,
            phoneNumber,
            dateOfBirth,
            countryCode,
            identityDocument,
            null,
            role,
            AccountStatus.PENDING_VERIFICATION,
            termsAccepted,
            now,
            now
        );
    }

    public static User reconstitute(
        UserId id, 
        Email email, 
        String passwordHash,
        Username username,
        String firstName,
        String lastName,
        Gender gender,
        PhoneNumber phoneNumber,
        LocalDate dateOfBirth,
        String countryCode,
        IdentityDocument identityDocument,
        String googleId,
        UserRole role,
        AccountStatus status,
        boolean termsAccepted,
        Instant termsAcceptedAt,
        Instant createdAt
    ) {
        return new User(
            id,
            email,
            passwordHash,
            username,
            firstName,
            lastName,
            gender,
            phoneNumber,
            dateOfBirth,
            countryCode,
            identityDocument,
            googleId,
            role,
            status,
            termsAccepted,
            termsAcceptedAt,
            createdAt
        );
    }

    public static User registerWithGoogle(GoogleUserInfo googleUserInfo) {
        Instant now = Instant.now();
        return new User(
            UserId.newId(),
            new Email(googleUserInfo.email()),
            null,
            null,
            googleUserInfo.firstName(),
            googleUserInfo.lastName(),
            null,
            null,
            null,
            null,
            null,
            googleUserInfo.googleId(),
            UserRole.RIDER,
            AccountStatus.PENDING_ONBOARDING,
            true,
            now,
            now);
    }

    public void linkGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public boolean isSuspended() {
        return status == AccountStatus.SUSPENDED;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean needsOnboarding() {
        return status == AccountStatus.PENDING_ONBOARDING;
    }
}
