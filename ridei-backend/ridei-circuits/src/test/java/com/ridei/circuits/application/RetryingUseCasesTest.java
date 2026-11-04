package com.ridei.circuits.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.ridei.circuits.domain.exception.CatalogItemAlreadyExistsException;
import com.ridei.circuits.domain.exception.ConcurrentCatalogModificationException;

class RetryingUseCasesTest {

    private final AtomicInteger attempts = new AtomicInteger();

    @Test
    void retriesOnceAfterLosingARaceOnAUniqueConstraint() {
        String result = RetryingUseCases.retryOnceOnConcurrentWrite(() -> {
            if (attempts.incrementAndGet() == 1) throw new CatalogItemAlreadyExistsException("lost", null);
            return "existing";
        });

        assertThat(result).isEqualTo("existing");
        assertThat(attempts).hasValue(2);
    }

    @Test
    void givesUpAfterTheSecondConcurrentFailure() {
        assertThatThrownBy(() -> RetryingUseCases.retryOnceOnConcurrentWrite(() -> {
            attempts.incrementAndGet();
            throw new ConcurrentCatalogModificationException("lost again", null);
        })).isInstanceOf(ConcurrentCatalogModificationException.class);
        assertThat(attempts).hasValue(2);
    }

    @Test
    void doesNotRetryBusinessRejections() {
        assertThatThrownBy(() -> RetryingUseCases.retryOnceOnConcurrentWrite(() -> {
            attempts.incrementAndGet();
            throw new IllegalArgumentException("invalid");
        })).isInstanceOf(IllegalArgumentException.class);
        assertThat(attempts).hasValue(1);
    }

}
