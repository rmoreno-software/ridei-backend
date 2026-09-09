package com.ridei.identity.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.ridei.identity.domain.model.AccountStatus;
import com.ridei.identity.domain.model.DocumentType;
import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.Gender;
import com.ridei.identity.domain.model.IdentityDocument;
import com.ridei.identity.domain.model.PhoneNumber;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserRole;
import com.ridei.identity.domain.model.Username;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name="first_name", nullable = false)
    private String firstName;

    @Column(name="last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Column
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "country_code")
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "terms_accepted", nullable = false)
    private boolean termsAccepted;

    @Column(name = "terms_accepted_at")
    private Instant termsAcceptedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "google_id", unique = true)
    private String googleId;
    
    @Column(name = "picture_url", unique = true)
    private String pictureUrl;

    @Column(name = "temporary_password_hash")
    private String temporaryPasswordHash;

    @Column(name = "temporary_password_expires_at")
    private Instant temporaryPasswordExpiresAt;

    @Column(name = "email_verification_token_hash")
    private String emailVerificationTokenHash;

    @Column(name = "email_verification_token_expires_at")
    private Instant emailVerificationTokenExpiresAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RiderProfileJpaEntity riderProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrganizerAccountJpaEntity organizerAccount;

    public static UserJpaEntity fromDomain(User user) {
        return UserJpaEntity.builder()
        .id(user.getId().value())
            .email(user.getEmail().value())
            .passwordHash(user.getPasswordHash())
            .username(user.getUsername() != null ? user.getUsername().value() : null)
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .gender(user.getGender())
            .phoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber().value() : null)
            .dateOfBirth(user.getDateOfBirth())
            .countryCode(user.getCountryCode())
            .documentType(user.getIdentityDocument() != null ? user.getIdentityDocument().type() : null)
            .documentNumber(user.getIdentityDocument() != null ? user.getIdentityDocument().number() : null)
            .googleId(user.getGoogleId())
            .role(user.getRole())
            .accountStatus(user.getStatus())
            .termsAccepted(user.isTermsAccepted())
            .termsAcceptedAt(user.getTermsAcceptedAt())
            .createdAt(user.getCreatedAt())
            .pictureUrl(user.getPictureUrl())
            .temporaryPasswordHash(user.getTemporaryPasswordHash())
            .temporaryPasswordExpiresAt(user.getTemporaryPasswordHashExpiresAt())
            .emailVerificationTokenHash(user.getEmailVerificationTokenHash())
            .emailVerificationTokenExpiresAt(user.getEmailVerificationTokenExpiresAt())
            .build();
    }

    public User toDomain() {
        return User.reconstitute(
                new UserId(this.id),
                new Email(this.email),
                this.passwordHash,
                this.username != null ? new Username(this.username) : null,
                this.firstName,
                this.lastName,
                this.gender,
                this.phoneNumber != null ? new PhoneNumber(this.phoneNumber) : null,
                this.dateOfBirth,
                this.countryCode,
                this.documentType != null ? new IdentityDocument(this.documentType, this.documentNumber) : null,
                this.googleId,
                this.role,
                this.accountStatus,
                this.termsAccepted,
                this.termsAcceptedAt,
                this.createdAt,
                this.pictureUrl,
                this.temporaryPasswordHash,
                this.temporaryPasswordExpiresAt,
                this.emailVerificationTokenHash,
                this.emailVerificationTokenExpiresAt
        );
    }
}
