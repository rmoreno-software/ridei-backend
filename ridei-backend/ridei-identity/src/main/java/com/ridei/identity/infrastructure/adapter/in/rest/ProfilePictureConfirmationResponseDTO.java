package com.ridei.identity.infrastructure.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfilePictureConfirmationResponseDTO {
    private String message;

    public static ProfilePictureConfirmationResponseDTO success() {
        return new ProfilePictureConfirmationResponseDTO("Profile picture updated successfully");
    }

    public static ProfilePictureConfirmationResponseDTO removed() {
        return new ProfilePictureConfirmationResponseDTO("Profile picture removed successfully");
    }
}
