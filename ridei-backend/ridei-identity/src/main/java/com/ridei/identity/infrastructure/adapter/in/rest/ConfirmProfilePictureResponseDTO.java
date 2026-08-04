package com.ridei.identity.infrastructure.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmProfilePictureResponseDTO {
    private String message;

    public static ConfirmProfilePictureResponseDTO success() {
        return new ConfirmProfilePictureResponseDTO("Profile picture updated successfully");
    }
}
