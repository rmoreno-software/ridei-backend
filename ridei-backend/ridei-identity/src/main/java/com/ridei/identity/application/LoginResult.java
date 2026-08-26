package com.ridei.identity.application;

import com.ridei.identity.domain.model.UserId;

public record LoginResult(
    UserId userId,
    String email,
    String accessToken,
    String refreshToken,
    boolean needsOnboarding,
    boolean mustChangePassword
) {}
