package com.ridei.identity.application;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

class EmailVerificationTokenFactory {

    private static final Duration TTL = Duration.ofHours(24);

    private final SecureRandom random = new SecureRandom();

    record IssuedToken(String rawToken, String hashToken, Instant expiresAt) {}

    IssuedToken issue() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new IssuedToken(rawToken, TokenHasher.sha256(rawToken), Instant.now().plus(TTL));
    }

    String buildVerificationLink(String publicApiUrl, String rawToken) {
        return publicApiUrl + "/api/v1/auth/verify-email?token=" + rawToken;
    }
    
}
