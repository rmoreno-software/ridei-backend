package com.ridei.circuits.infrastructure.adapter.out.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class CachingTokenValidatorTest {

    private static final Duration TTL = Duration.ofSeconds(60);

    private final AtomicLong nanos = new AtomicLong();
    private final AtomicInteger calls = new AtomicInteger();
    private final Deque<TokenValidationStatus> answers = new ArrayDeque<>();
    private final TokenValidator identity = token -> {
        calls.incrementAndGet();
        return answers.isEmpty() ? TokenValidationStatus.VALID : answers.pop();
    };
    private final CachingTokenValidator validator = new CachingTokenValidator(identity, TTL, 100, nanos::get);

    @Test
    void validVerdictsAreCachedDuringTheTtl() {
        validator.validate("token");
        validator.validate("token");

        assertThat(calls).hasValue(1);
    }

    @Test
    void aRevocationIsSeenOnceTheTtlExpires() {
        answers.add(TokenValidationStatus.VALID);
        answers.add(TokenValidationStatus.REVOKED);
        validator.validate("token");

        nanos.addAndGet(TTL.plusSeconds(1).toNanos());

        assertThat(validator.validate("token")).isEqualTo(TokenValidationStatus.REVOKED);
        assertThat(calls).hasValue(2);
    }

    @Test
    void revokedVerdictsAreCachedToo() {
        answers.add(TokenValidationStatus.REVOKED);

        validator.validate("token");

        assertThat(validator.validate("token")).isEqualTo(TokenValidationStatus.REVOKED);
        assertThat(calls).hasValue(1);
    }

    @Test
    void unavailabilityIsNeverCached() {
        answers.add(TokenValidationStatus.UNAVAILABLE);

        assertThat(validator.validate("token")).isEqualTo(TokenValidationStatus.UNAVAILABLE);
        assertThat(validator.validate("token")).isEqualTo(TokenValidationStatus.VALID);
        assertThat(calls).hasValue(2);
    }

    @Test
    void differentTokensAreValidatedSeparately() {
        validator.validate("token-a");
        validator.validate("token-b");

        assertThat(calls).hasValue(2);
    }
}
