package com.ridei.identity.application;

import java.util.Locale;

import com.ridei.identity.domain.model.UserId;

public record ChangePasswordCommand(
    UserId userId,
    String newPassword,
    Locale locale
) {}
