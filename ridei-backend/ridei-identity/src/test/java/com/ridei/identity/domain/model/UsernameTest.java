package com.ridei.identity.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Username value object")
class UsernameTest {

    @Test
    @DisplayName("accepts a valid username")
    void shouldAcceptValidUsername() {
        assertThatNoException().isThrownBy(() -> new Username("@crazyRider69"));
    }

    @ParameterizedTest
    @DisplayName("rejects invalid usernames")
    @ValueSource(strings = {
        "noatsign",
        "@ab",
        "@",
        "@this_username_is_way_too_long_to_be_valid",
        "@invalid name"
    })
    void shouldRejectInvalidUsername(String invalid) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Username(invalid));
    }

}
