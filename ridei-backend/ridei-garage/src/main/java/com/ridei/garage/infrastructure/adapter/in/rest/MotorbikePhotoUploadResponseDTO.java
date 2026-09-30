package com.ridei.garage.infrastructure.adapter.in.rest;

import com.ridei.garage.domain.model.PresignedUpload;

public record MotorbikePhotoUploadResponseDTO(
    String uploadUrl,
    String publicUrl,
    String expiresAt
) {
    public static MotorbikePhotoUploadResponseDTO fromDomain(PresignedUpload upload) {
        return new MotorbikePhotoUploadResponseDTO(
            upload.uploadUrl(),
            upload.publicUrl(),
            upload.expiresAt().toString()
        );
    }
}
