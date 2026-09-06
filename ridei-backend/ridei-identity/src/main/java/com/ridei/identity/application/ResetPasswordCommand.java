package com.ridei.identity.application;

import com.ridei.identity.domain.model.Email;

public record ResetPasswordCommand(
    Email email,
    String temporaryPassword,
    String newPassword
) {}