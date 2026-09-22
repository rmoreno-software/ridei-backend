package com.ridei.identity.infrastructure.adapter.out.security;

import org.junit.jupiter.api.Test;

import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtAdapterTest {
    
    private final JwtAdapter jwt = new JwtAdapter("a-test-private-key-with-at-least-32-bytes!", "a-test-public-key-with-at-least-32-bytes!", 60_000, 120_000);
    private final UserId userId = UserId.newId();

    @Test
    void accessTokenIsValidOnlyAsAccessToken() {
        String token = jwt.generateAccessToken(userId, UserRole.RIDER, 0);

        assertThat(jwt.validateAccessToken(token)).isTrue();
        assertThat(jwt.validateRefreshToken(token)).isFalse();
    }

    @Test
    void refreshTokenIsValidOnlyAsRefreshToken() {
        String token = jwt.generateRefreshToken(userId, 0);

        assertThat(jwt.validateRefreshToken(token)).isTrue();
        assertThat(jwt.validateAccessToken(token)).isFalse();
    }

    @Test
    void garbageTokenIsNeverValid() {
        assertThat(jwt.validateAccessToken("not-a-jwt")).isFalse();
        assertThat(jwt.validateRefreshToken("not-a-jwt")).isFalse();
    }
}
