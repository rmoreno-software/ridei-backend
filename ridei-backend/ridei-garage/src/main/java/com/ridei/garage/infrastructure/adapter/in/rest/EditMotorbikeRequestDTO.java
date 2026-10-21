package com.ridei.garage.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.ridei.garage.application.EditMotorbikeCommand;
import com.ridei.garage.domain.model.BrandId;
import com.ridei.garage.domain.model.Category;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class EditMotorbikeRequestDTO {

    private UUID brandId;

    @Size(max = 100)
    private String customBrandName;

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

    @PastOrPresent
    private LocalDate disposalDate;

    @AssertTrue(message = "Provide either brandId or customBrandName, not both")
    private boolean isBrandChoiceValid() {
        boolean hasBrandId = brandId != null;
        boolean hasCustomName = customBrandName != null && !customBrandName.isBlank();
        return hasBrandId ^ hasCustomName;
    }

    public EditMotorbikeCommand toCommand(OwnerId ownerId, MotorbikeId motorbikeId) {
        return new EditMotorbikeCommand(
            ownerId,
            motorbikeId,
            brandId != null ? new BrandId(brandId) : null,
            customBrandName,
            model,
            category,
            year,
            displacementCc,
            weightKg,
            acquisitionDate,
            disposalDate
        );
    }

}
