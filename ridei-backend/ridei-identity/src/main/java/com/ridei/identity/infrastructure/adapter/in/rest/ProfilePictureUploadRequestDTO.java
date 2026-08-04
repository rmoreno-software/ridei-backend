package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProfilePictureUploadRequestDTO {
    @NotBlank(message = "Content type is required")
    @Pattern(regexp = "^image/(jpeg|png|webp)$", message = "Unsupported image content type")
    private String contentType;
}
