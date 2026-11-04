package com.ridei.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

class ValidateTokenServiceTest {

    private static final String TOKEN = "access-token";
    private static final UserId USER_ID = new UserId(UUID.randomUUID());

    private final JwtPort jwt = mock(JwtPort.class);
    private final UserRepositoryPort users = mock(UserRepositoryPort.class);
    private final User user = mock(User.class);
    private final ValidateTokenService service = new ValidateTokenService(jwt, users);

    @BeforeEach
    void setUp() {
        when(jwt.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwt.extractUserId(TOKEN)).thenReturn(USER_ID);
        when(jwt.extractTokenVersion(TOKEN)).thenReturn(3);
        when(users.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.isActive()).thenReturn(true);
        when(user.getTokenVersion()).thenReturn(3);
    }

    @Test
    void acceptsATokenOfAnActiveUserWithTheCurrentTokenVersion() {
        assertThat(service.validate(TOKEN)).isTrue();
    }

    @Test
    void rejectsATokenRevokedByAPasswordChange() {
        when(user.getTokenVersion()).thenReturn(4);

        assertThat(service.validate(TOKEN)).isFalse();
    }

    @Test
    void rejectsATokenOfAnInactiveUser() {
        when(user.isActive()).thenReturn(false);

        assertThat(service.validate(TOKEN)).isFalse();
    }

    @Test
    void rejectsATokenOfAnUnknownUser() {
        when(users.findById(USER_ID)).thenReturn(Optional.empty());

        assertThat(service.validate(TOKEN)).isFalse();
    }

    @Test
    void rejectsAnInvalidSignatureWithoutTouchingTheDatabase() {
        when(jwt.validateAccessToken(TOKEN)).thenReturn(false);

        assertThat(service.validate(TOKEN)).isFalse();
        verifyNoInteractions(users);
    }
}
