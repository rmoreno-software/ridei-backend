package com.ridei.identity.infrastructure.adapter.out.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtAdapterTest {
    
    private final JwtAdapter jwt = newAdapterWithThrowawayKeys();
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

    /** Genera un par de claves EC P-256 desechable, codificado igual que en configuración (Base64 DER). */
    private static JwtAdapter newAdapterWithThrowawayKeys() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();
            Base64.Encoder base64 = Base64.getEncoder();
            return new JwtAdapter(
                base64.encodeToString(keyPair.getPrivate().getEncoded()),
                base64.encodeToString(keyPair.getPublic().getEncoded()),
                60_000,
                120_000
            );
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate test key pair", e);
        }
    }
}
