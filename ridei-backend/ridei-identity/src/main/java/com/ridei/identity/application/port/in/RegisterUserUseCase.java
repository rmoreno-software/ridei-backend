package com.ridei.identity.application.port.in;

public interface RegisterUserUseCase {
    void register(String email, String password, String name);
}
