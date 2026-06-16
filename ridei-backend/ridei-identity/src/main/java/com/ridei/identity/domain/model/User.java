package com.ridei.identity.domain.model;

import java.time.Instant;
import java.time.LocalDate;

import com.ridei.identity.application.RegisterUserCommand;
import com.ridei.identity.domain.exception.MinimumAgeNotMetException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
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
    private UserRole role;
    private AccountStatus status;
    private boolean termsAccepted;
    private Instant termsAcceptedAt;
    private Instant createdAt;

    public static User register(RegisterUserCommand cmd, String passwordHash) {
        if (cmd.dateOfBirth().isAfter(LocalDate.now().minusYears(16)))
            throw new MinimumAgeNotMetException();
        Instant now = Instant.now();
        return new User(
            UserId.newId(),
            cmd.email(),
            passwordHash,
            cmd.username(),
            cmd.firstName(),
            cmd.lastName(),
            cmd.gender(),
            cmd.phoneNumber(),
            cmd.dateOfBirth(),
            cmd.countryCode(),
            cmd.identityDocument(),
            cmd.role(),
            AccountStatus.PENDING_VERIFICATION,
            cmd.termsAccepted(),
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
            role,
            status,
            termsAccepted,
            termsAcceptedAt,
            createdAt
        );
}

}
