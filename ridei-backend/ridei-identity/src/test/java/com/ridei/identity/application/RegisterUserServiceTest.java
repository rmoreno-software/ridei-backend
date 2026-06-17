package com.ridei.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ridei.identity.domain.exception.EmailAlreadyRegisteredException;
import com.ridei.identity.domain.exception.UsernameAlreadyTakenException;
import com.ridei.identity.domain.model.DocumentType;
import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.Gender;
import com.ridei.identity.domain.model.IdentityDocument;
import com.ridei.identity.domain.model.PhoneNumber;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserRole;
import com.ridei.identity.domain.model.Username;
import com.ridei.identity.domain.port.out.EventPublisherPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserService")
class RegisterUserServiceTest {

    @Mock private UserRepositoryPort repository;
    @Mock private EventPublisherPort eventPublisher;
    @Mock private PasswordHasherPort passwordHasher;

    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(repository, eventPublisher, passwordHasher);
    }

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
    @DisplayName("registers a user and returns a new UserId")
    void shouldRegisterUserSuccessfully() {
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.existsByUsername(any())).thenReturn(false);
        when(passwordHasher.hash(any())).thenReturn("hashed_password");

        UserId id = service.register(validCommand());

        assertThat(id).isNotNull();
        verify(repository, times(1)).save(any());
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    @DisplayName("throws EmailAlreadyRegisteredException if email is taken")
    void shouldThrowWhenEmailAlreadyExists() {
        when(repository.existsByEmail(any())).thenReturn(true);

        assertThatExceptionOfType(EmailAlreadyRegisteredException.class)
                .isThrownBy(() -> service.register(validCommand()));

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("throws UsernameAlreadyTakenException if username is taken")
    void shouldThrowWhenUsernameAlreadyExists() {
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.existsByUsername(any())).thenReturn(true);

        assertThatExceptionOfType(UsernameAlreadyTakenException.class)
                .isThrownBy(() -> service.register(validCommand()));

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("never stores the raw password")
    void shouldHashPasswordBeforeSaving() {
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.existsByUsername(any())).thenReturn(false);
        when(passwordHasher.hash("Secure1234")).thenReturn("$2a$10$hashed");

        service.register(validCommand());

        verify(passwordHasher, times(1)).hash("Secure1234");
        verify(repository).save(argThat(user ->
                user.getPasswordHash().equals("$2a$10$hashed")
        ));
    }
}
