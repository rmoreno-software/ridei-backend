package com.ridei.identity.application;

import com.ridei.identity.domain.model.ImageContentType;
import com.ridei.identity.domain.model.UserId;

public record RequestProfilePictureUploadCommand(
    UserId userId,
    ImageContentType contentType
) {}
