package com.ridei.garage.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data 
public class ConfirmMotorbikePhotoRequestDTO {
    @NotBlank 
    @Pattern (regexp = "^https://.+", message = "Invalid public URL")
    private String publicUrl;
}
