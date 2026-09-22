package com.ridei.identity.infrastructure.adapter.out.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserRole;
import com.ridei.identity.domain.port.out.JwtPort;

import io.jsonwebtoken.Jwts;

@Component
public class JwtAdapter implements JwtPort {

    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtAdapter(
        @Value("${jwt.private-key}") String privateKeyBase64,
        @Value("${jwt.public-key}") String publicKeyBase64,
        @Value("${jwt.expiration-ms}") long expirationMs,
        @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.privateKey = loadPrivateKey(privateKeyBase64);
        this.publicKey = loadPublicKey(publicKeyBase64);
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Override
    public String generateAccessToken(UserId userId, UserRole role, int tokenVersion) {
        return Jwts.builder()
                .subject(userId.value().toString())
                .claim("role", role.name())
                .claim("type", TYPE_ACCESS)
                .claim("tv", tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
    }

    @Override
    public String generateRefreshToken(UserId userId, int tokenVersion) {
        return Jwts.builder()
                .subject(userId.value().toString())
                .claim("type", TYPE_REFRESH)
                .claim("tv", tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
    }

    @Override
    public boolean validateAccessToken(String token) {
        return isValidTokenOfType(token, TYPE_ACCESS);
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return isValidTokenOfType(token, TYPE_REFRESH);
    }

    private boolean isValidTokenOfType(String token, String expectedType) {
        try {
            String type = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
            return expectedType.equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UserId extractUserId(String token) {
        String subject = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
        return UserId.of(subject);
    }

    @Override
    public String extractRole(String token) {
        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("role", String.class);
    }

    @Override
    public int extractTokenVersion(String token) {
        Object tv = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("tv");
        return tv == null ? 0 : ((Number) tv).intValue();
    }

    private static PrivateKey loadPrivateKey(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT private key configuration", e);
        }
    }

    private static PublicKey loadPublicKey(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT public key configuration", e);
        }
    }
}
