package com.ridei.identity.application;

public record GoogleAuthResult(
    String accessToken,
    String refreshToken,
    boolean needsOnboarding
) {}
