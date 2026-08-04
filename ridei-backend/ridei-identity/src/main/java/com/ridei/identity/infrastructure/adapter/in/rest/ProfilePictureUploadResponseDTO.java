package com.ridei.identity.infrastructure.adapter.in.rest;

import com.ridei.identity.domain.model.PresignedUpload;

public record ProfilePictureUploadResponseDTO(
    String uploadUrl,
    String publicUrl,
    String expiresAt
) {
    public static ProfilePictureUploadResponseDTO fromDomain(PresignedUpload upload) {
        return new ProfilePictureUploadResponseDTO(
            upload.uploadUrl(),
            upload.publicUrl(),
            upload.expiresAt().toString()
        );
    }
}
