package com.ridei.garage.infrastructure.adapter.in.rest;

import com.ridei.garage.domain.model.FocalPoint;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data 
public class UpdateFocalPointRequestDTO {
    
    @NotNull 
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double focalX;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double focalY;

    public FocalPoint toFocalPoint() {
        return new FocalPoint(focalX, focalY);
    }
}
