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
    private String temporaryPasswordHash;
    private Instant temporaryPasswordHashExpiresAt;
    private String emailVerificationTokenHash;
    private Instant emailVerificationTokenExpiresAt;

    public static User register(
        Email email,
        String passwordHash
    ) {
        Instant now = Instant.now();
        return new User(
            UserId.newId(),
            email,
            passwordHash,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            UserRole.RIDER,
            AccountStatus.PENDING_VERIFICATION,
            false,
            null,
            now,
            null,
            null,
            null,
            null,
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
        String pictureUrl,
        String temporaryPasswordHash,
        Instant temporaryPasswordHashExpiresAt,
        String emailVerificationTokenHash,
        Instant emailVerificationTokenExpiresAt
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
            pictureUrl,
            temporaryPasswordHash,
            temporaryPasswordHashExpiresAt,
            emailVerificationTokenHash,
            emailVerificationTokenExpiresAt
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
            null,
            null,
            null,
            null,
            null
        );
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

    public void removePictureUrl() {
        this.pictureUrl = null;
    }

    public void saveOnboardingStep4(IdentityDocument identityDocument) {
        if (identityDocument == null)
            throw new IllegalArgumentException("Identity document is required");
        this.identityDocument = identityDocument;
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

    public void acceptTerms() {
        this.termsAccepted = true;
        this.termsAcceptedAt = Instant.now();
        if (this.status == AccountStatus.PENDING_ONBOARDING) {
            this.status = AccountStatus.ACTIVE;
        }
    }

    public void issueTemporaryPassword(
        String temporaryPasswordHash,
        Instant expiresAt
    ) {
        if (temporaryPasswordHash == null || temporaryPasswordHash.isBlank())
            throw new IllegalArgumentException("Temporary password hash is required");
        this.temporaryPasswordHash = temporaryPasswordHash;
        this.temporaryPasswordHashExpiresAt = expiresAt;
    }

    public boolean hasValidTemporaryPassword() {
        return temporaryPasswordHash != null
            && temporaryPasswordHashExpiresAt != null
            && Instant.now().isBefore(temporaryPasswordHashExpiresAt);
    }

    public void clearTemporaryPassword() {
        this.temporaryPasswordHash = null;
        this.temporaryPasswordHashExpiresAt = null;
    }

    public void changePassword(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank())
            throw new IllegalArgumentException("Password hash is required");
        this.passwordHash = passwordHash;
        clearTemporaryPassword();
    }

    public void issueEmailVerificationToken(String tokenHash, Instant expiresAt) {
        if (tokenHash == null || tokenHash.isBlank())
            throw new IllegalArgumentException("Email verification token hash is required");

        this.emailVerificationTokenHash = tokenHash;
        this.emailVerificationTokenExpiresAt = expiresAt;
    }

    public boolean hasValidEmailVerificationToken() {
        return emailVerificationTokenHash != null
            && emailVerificationTokenExpiresAt != null
            && Instant.now().isBefore(emailVerificationTokenExpiresAt);
    }

    public void verifyEmail() {
        this.emailVerificationTokenHash = null;
        this.emailVerificationTokenExpiresAt = null;
        if (this.status == AccountStatus.PENDING_VERIFICATION) {
            this.status = AccountStatus.PENDING_ONBOARDING;
        }
    }
}
