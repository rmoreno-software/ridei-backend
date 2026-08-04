package com.ridei.identity.application;

import com.ridei.identity.domain.model.UserId;

public record ConfirmProfilePictureCommand(
    UserId userId,
    String publicUrl
) {}
