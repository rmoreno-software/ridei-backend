package com.ridei.garage.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data 
public class MotorbikePhotoUploadRequestDTO {
    @NotBlank 
    @Pattern(regexp = "^image/(jpeg|png|webp)$", message = "Unsupported content type")
    private String contentType;
}