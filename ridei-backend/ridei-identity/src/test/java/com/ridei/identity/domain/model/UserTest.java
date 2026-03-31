package com.ridei.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure Unit Tests for the User Domain Entity.
 * Execution time: <10ms. No Spring Context required.
 */
class UserTest {

    @Test
    @DisplayName("Should create a new user and generate a unique UUID")
    void shouldCreateNewUserWithGeneratedId() {
        // Arrange
        String email = "john.doe@example.com";
        String password = "hashed_password_123";
        String name = "John Doe";
        String nickname = "johndoe99";

        User newUser = User.create(email, nickname, password, name);

        // Assert
        assertThat(newUser).isNotNull();
        assertThat(newUser.getId()).isNotNull();
        assertThat(newUser.getEmail()).isEqualTo(email);
        assertThat(newUser.getPassword()).isEqualTo(password);
        assertThat(newUser.getName()).isEqualTo(name);
        assertThat(newUser.getNickname()).isEqualTo(nickname);
    }
}
