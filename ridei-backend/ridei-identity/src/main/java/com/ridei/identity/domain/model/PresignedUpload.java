package com.ridei.identity.domain.model;

import java.time.Instant;

public record PresignedUpload(
    String uploadUrl,
    String publicUrl,
    Instant expiresAt
) {}
