package com.ridei.identity.domain.model;

public record RegisterResult(
    UserId userId,
    String email,
    String accessToken,
    String refreshToken,
    boolean needsOnboarding
) {}
