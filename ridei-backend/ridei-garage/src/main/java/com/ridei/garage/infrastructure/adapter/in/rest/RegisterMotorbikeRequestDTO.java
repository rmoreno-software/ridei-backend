package com.ridei.garage.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ridei.garage.application.RegisterMotorbikeCommand;
import com.ridei.garage.domain.model.Category;
import com.ridei.garage.domain.model.OwnerId;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class RegisterMotorbikeRequestDTO {
    
    @NotBlank
    @Size(max = 100)
    private String brand;

    @NotBlank
    @Size(max = 100)
    private String model;

    @NotNull 
    private Category category;

    @NotNull 
    @Min(1885)
    private Integer year;

    @Positive
    private Integer displacementCc;

    @Positive
    private BigDecimal weightKg;

    @PastOrPresent
    private LocalDate acquisitionDate;

    private boolean active;

    public RegisterMotorbikeCommand toCommand(OwnerId ownerId) {
        return new RegisterMotorbikeCommand(
            ownerId,
            brand,
            model,
            category,
            year,
            displacementCc,
            weightKg,
            acquisitionDate,
            active
        );
    }

}
