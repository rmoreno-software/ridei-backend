package com.ridei.identity.domain.model;

import java.util.UUID;

import lombok.Getter;

@Getter
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
        return new User(UUID.randomUUID(), email, password, name);
    }
}
