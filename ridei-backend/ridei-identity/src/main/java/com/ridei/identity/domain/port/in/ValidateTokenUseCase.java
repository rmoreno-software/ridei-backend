package com.ridei.identity.domain.port.in;

public interface ValidateTokenUseCase {
    boolean validate(String token);
}
