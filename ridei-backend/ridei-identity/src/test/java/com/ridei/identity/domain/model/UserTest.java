package com.ridei.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ridei.identity.domain.exception.MinimumAgeNotMetException;

@DisplayName("User aggregate")
class UserTest {

    @Test
    @DisplayName("registers a valid user with PENDING_VERIFICATION status")
    void shouldRegisterValidUser() {
        User user = User.register(
            new Email("test@ridei.com"),
            "hashed_password",
            new Username("@crazyRider69"),
            "Marc",
            "Marquez",
            Gender.MALE,
            new PhoneNumber("+34612345678"),
            LocalDate.of(1993, 2, 17),
            "ES",
            new IdentityDocument(DocumentType.NATIONAL_ID, "12345678A"),
            UserRole.RIDER,
            true
        );

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail().value()).isEqualTo("test@ridei.com");
        assertThat(user.getUsername().value()).isEqualTo("@crazyRider69");
        assertThat(user.getStatus()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("rejects users under 16 years old")
    void shouldRejectUnderageUser() {
        assertThatExceptionOfType(MinimumAgeNotMetException.class)
            .isThrownBy(() -> User.register(
                new Email("young@ridei.com"),
                "hashed_password",
                new Username("@youngRider"),
                "Young",
                "Rider",
                Gender.MALE,
                new PhoneNumber("+34612345678"),
                LocalDate.now().minusYears(15),  // 15 años
                "ES",
                new IdentityDocument(DocumentType.NATIONAL_ID, "12345678A"),
                UserRole.RIDER,
                true
            ));
    }

    @Test
    @DisplayName("stores the provided password hash, not the raw password")
    void shouldStoreHashedPassword() {
        User user = User.register(
            new Email("test@ridei.com"),
            "bcrypt_hashed_value",
            new Username("@crazyRider69"),
            "Marc",
            "Marquez",
            Gender.MALE,
            new PhoneNumber("+34612345678"),
            LocalDate.of(1993, 2, 17),
            "ES",
            new IdentityDocument(DocumentType.NATIONAL_ID, "12345678A"),
            UserRole.RIDER,
            true
        );

        assertThat(user.getPasswordHash()).isEqualTo("bcrypt_hashed_value");
        assertThat(user.getPasswordHash()).doesNotContain("Secure1234");
    }

    @Test
    @DisplayName("reports suspended, active and onboarding status correctly")
    void shouldExposeStatusQueries() {
        User active = User.reconstitute(
            UserId.newId(), new Email("a@ridei.com"), "hash", new Username("@aaaa"),
            "A", "A", Gender.MALE, null, LocalDate.of(1990, 1, 1), "ES", null,
            null, UserRole.RIDER, AccountStatus.ACTIVE, true, null, null, null
        );
        User suspended = User.reconstitute(
            UserId.newId(), new Email("b@ridei.com"), "hash", new Username("@bbbb"),
            "B", "B", Gender.MALE, null, LocalDate.of(1990, 1, 1), "ES", null,
            null, UserRole.RIDER, AccountStatus.SUSPENDED, true, null, null, null
        );
        User pendingOnboarding = User.registerWithGoogle(
            new GoogleUserInfo("google-id", "c@ridei.com", "C", "C", null)
        );

        assertThat(active.isActive()).isTrue();
        assertThat(active.isSuspended()).isFalse();

        assertThat(suspended.isSuspended()).isTrue();
        assertThat(suspended.isActive()).isFalse();

        assertThat(pendingOnboarding.needsOnboarding()).isTrue();
    }

}
