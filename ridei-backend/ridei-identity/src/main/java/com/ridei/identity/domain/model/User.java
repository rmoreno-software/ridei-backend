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
    private String pictureUrl;

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
            termsAccepted == true ? now : null,
            now,
            null
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
        Instant createdAt,
        String pictureUrl
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
            createdAt,
            pictureUrl
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
            false,
            null,
            now,
            null);
    }

    public void saveOnboardingStep1(String firstName, String lastName, Username username, Gender gender) {
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name is required");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name is required");
        if (username == null)
            throw new IllegalArgumentException("Username is required");

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.gender = gender;
    }

    public void saveOnboardingStep2(
        LocalDate dateOfBirth,
        String countryCode,
        PhoneNumber phoneNumber
    ) {
        if (dateOfBirth == null)
            throw new IllegalArgumentException("Date of birth is required");
        if (dateOfBirth.isAfter(LocalDate.now().minusYears(16)))
            throw new MinimumAgeNotMetException();
        if (countryCode == null || countryCode.isBlank())
            throw new IllegalArgumentException("Country code is required");
        if (phoneNumber == null)
            throw new IllegalArgumentException("Phone number is required");

        this.dateOfBirth = dateOfBirth;
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
    }

    public void updateProfilePicture(String profilePictureUrl) {
        if (profilePictureUrl == null || profilePictureUrl.isBlank())
            throw new IllegalArgumentException("Profile Picture URL is required");
        this.pictureUrl = profilePictureUrl;
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
