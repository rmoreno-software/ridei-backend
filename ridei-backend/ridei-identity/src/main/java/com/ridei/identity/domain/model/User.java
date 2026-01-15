package com.ridei.identity.domain.model;

import java.util.UUID;

public class User {

    private final UUID id;
    private final String email;
    private final String password;
    private final String name;

    private User(UUID id, String email, String password, String name) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public static User create(String email, String password, String name) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("The email address is invalid");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("The password must be at least 6 characters long");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The name is required");
        }

        return new User(UUID.randomUUID(), email, password, name);
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
}
