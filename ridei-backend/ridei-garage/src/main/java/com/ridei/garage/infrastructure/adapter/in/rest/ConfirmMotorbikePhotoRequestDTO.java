package com.ridei.garage.infrastructure.adapter.in.rest;

import com.ridei.garage.domain.model.FocalPoint;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data 
public class ConfirmMotorbikePhotoRequestDTO {
    @NotBlank 
    @Pattern (regexp = "^https://.+", message = "Invalid public URL")
    private String publicUrl;

    @DecimalMin("0.0") 
    @DecimalMax ("1.0")
    private Double focalX;

    @DecimalMin("0.0") 
    @DecimalMax ("1.0")
    private Double focalY;

    public FocalPoint focalPoint() {
        return new FocalPoint(
            focalX != null ? focalX : FocalPoint.CENTER.x(),
            focalY != null ? focalY : FocalPoint.CENTER.y()
        );
    }
}
