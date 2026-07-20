package com.ridei.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ridei.identity.domain.model.UsernameAvailabilityReason;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckUsernameAvailabilityService")
class CheckUsernameAvailabilityServiceTest {

    @Mock private UserRepositoryPort repository;

    private CheckUsernameAvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new CheckUsernameAvailabilityService(repository);
    }

    @Test
    @DisplayName("is available when the format is valid and no one has taken it")
    void shouldBeAvailable() {
        when(repository.existsByUsername(any())).thenReturn(false);

        UsernameAvailabilityResult result = service.check("crazyRider69");

        assertThat(result.available()).isTrue();
        assertThat(result.reason()).isEqualTo(UsernameAvailabilityReason.AVAILABLE);
    }

    @Test
    @DisplayName("is unavailable when another user already has it")
    void shouldBeUnavailableWhenAlreadyTaken() {
        when(repository.existsByUsername(any())).thenReturn(true);

        UsernameAvailabilityResult result = service.check("crazyRider69");

        assertThat(result.available()).isFalse();
        assertThat(result.reason()).isEqualTo(UsernameAvailabilityReason.ALREADY_TAKEN);
    }

    @Test
    @DisplayName("is unavailable when the format is invalid, without querying the repository")
    void shouldBeUnavailableWhenFormatIsInvalid() {
        UsernameAvailabilityResult result = service.check("ab");

        assertThat(result.available()).isFalse();
        assertThat(result.reason()).isEqualTo(UsernameAvailabilityReason.INVALID_FORMAT);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("is unavailable for blank input")
    void shouldBeUnavailableWhenBlank() {
        UsernameAvailabilityResult result = service.check("   ");

        assertThat(result.available()).isFalse();
        assertThat(result.reason()).isEqualTo(UsernameAvailabilityReason.INVALID_FORMAT);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("is unavailable for null input")
    void shouldBeUnavailableWhenNull() {
        UsernameAvailabilityResult result = service.check(null);

        assertThat(result.available()).isFalse();
        assertThat(result.reason()).isEqualTo(UsernameAvailabilityReason.INVALID_FORMAT);
        verifyNoInteractions(repository);
    }

}
