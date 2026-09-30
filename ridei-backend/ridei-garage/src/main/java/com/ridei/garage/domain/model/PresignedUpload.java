package com.ridei.garage.domain.model;

import java.time.Instant;

public record PresignedUpload(
    String uploadUrl,
    String publicUrl,
    Instant expiresAt
) {}
