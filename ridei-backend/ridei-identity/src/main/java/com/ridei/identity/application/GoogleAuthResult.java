package com.ridei.identity.application;

public record GoogleAuthResult(
    String accessToken,
    String refreshToken,
    String email,
    boolean needsOnboarding
) {}
