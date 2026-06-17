package com.ridei.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ridei.identity.application.RegisterUserCommand;
import com.ridei.identity.domain.exception.MinimumAgeNotMetException;

@DisplayName("User aggregate")
class UserTest {

    private RegisterUserCommand validCommand() {
        return new RegisterUserCommand(
            new Email("test@ridei.com"),
            "Secure1234",
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
    }

    @Test
    @DisplayName("registers a valid user with PENDING_VERIFICATION status")
    void shouldRegisterValidUser() {
        User user = User.register(validCommand(), "hashed_password");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail().value()).isEqualTo("test@ridei.com");
        assertThat(user.getUsername().value()).isEqualTo("@crazyRider69");
        assertThat(user.getStatus()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("rejects users under 16 years old")
    void shouldRejectUnderageUser() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                new Email("young@ridei.com"),
                "Secure1234",
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
        );

        assertThatExceptionOfType(MinimumAgeNotMetException.class)
                .isThrownBy(() -> User.register(cmd, "hashed_password"));
    }

    @Test
    @DisplayName("stores the provided password hash, not the raw password")
    void shouldStoreHashedPassword() {
        User user = User.register(validCommand(), "bcrypt_hashed_value");

        assertThat(user.getPasswordHash()).isEqualTo("bcrypt_hashed_value");
        assertThat(user.getPasswordHash()).doesNotContain("Secure1234");
    }

}
