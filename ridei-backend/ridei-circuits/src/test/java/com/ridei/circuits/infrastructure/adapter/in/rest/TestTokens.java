package com.ridei.circuits.infrastructure.adapter.in.rest;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.Jwts;

/** Par de claves ES256 de prueba y tokens con la misma forma que los de ridei-identity. */
final class TestTokens {

    static final UUID RIDER_ID = UUID.fromString("00000000-0000-4000-8000-0000000000aa");
    static final UUID ADMIN_ID = UUID.fromString("00000000-0000-4000-8000-0000000000bb");

    private static final KeyPair KEYS = generate();

    private TestTokens() {
    }

    static String publicKeyBase64() {
        return Base64.getEncoder().encodeToString(KEYS.getPublic().getEncoded());
    }

    static String rider() {
        return bearer(RIDER_ID, "RIDER", "access");
    }

    static String admin() {
        return bearer(ADMIN_ID, "ADMIN", "access");
    }

    static String refreshToken() {
        return bearer(RIDER_ID, null, "refresh");
    }

    private static String bearer(UUID subject, String role, String type) {
        var builder = Jwts.builder()
            .subject(subject.toString())
            .claim("type", type)
            .claim("tv", 0)
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plusSeconds(600)));
        if (role != null) builder.claim("role", role);
        return "Bearer " + builder.signWith(KEYS.getPrivate()).compact();
    }

    private static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
