package com.ridei.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;


@DisplayName("Email value object")
class EmailTest {

    @Test
    @DisplayName("accepts a valid email")
    void shouldAcceptValidEmail() {
        assertThatNoException().isThrownBy(() -> new Email("test@ridei.com"));
    }

    @ParameterizedTest
    @DisplayName("rejects invalid emails")
    @ValueSource(strings = {"notanemail", "@nodomain", "missing@", "", "spaces @ridei.com"})
    void shouldRejectInvalidEmial(String invalid) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Email(invalid));
    }

    @Test
    @DisplayName("rejects null")
    void shouldRejectNull() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Email(null));
    }
}
