package com.ridei.identity.application.port.in;

public interface ValidateTokenUseCase {
    boolean validate(String token);
}
