package com.ridei.circuits.infrastructure.adapter.out.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

/**
 * Cachea los veredictos definitivos (VALID / REVOKED) durante {@code ttl}, así que una
 * revocación tarda como mucho {@code ttl} en surtir efecto. {@code UNAVAILABLE} no se cachea:
 * en cuanto identity vuelve, la siguiente petición se valida de verdad.
 *
 * <p>La clave es el SHA-256 del token: los tokens en claro no se guardan en memoria.
 */
public class CachingTokenValidator implements TokenValidator {

    private final TokenValidator delegate;
    private final Cache<String, TokenValidationStatus> verdicts;

    public CachingTokenValidator(TokenValidator delegate, Duration ttl, long maxSize) {
        this(delegate, ttl, maxSize, Ticker.systemTicker());
    }

    CachingTokenValidator(TokenValidator delegate, Duration ttl, long maxSize, Ticker ticker) {
        this.delegate = delegate;
        this.verdicts = Caffeine.newBuilder().expireAfterWrite(ttl).maximumSize(maxSize).ticker(ticker).build();
    }

    @Override
    public TokenValidationStatus validate(String accessToken) {
        String key = sha256(accessToken);
        TokenValidationStatus cached = verdicts.getIfPresent(key);
        if (cached != null) return cached;

        TokenValidationStatus verdict = delegate.validate(accessToken);
        if (verdict != TokenValidationStatus.UNAVAILABLE) verdicts.put(key, verdict);
        return verdict;
    }

    private static String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
