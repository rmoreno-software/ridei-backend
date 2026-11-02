package com.ridei.garage.infrastructure.adapter.in.rest;

import com.ridei.garage.domain.model.FinalDriveRatio;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinalDriveRequestDTO {

    @NotNull(message = "Front sprocket teeth are required")
    @Min(1)
    @Max(FinalDriveRatio.MAX_TEETH)
    private Integer frontSprocket;

    @NotNull(message = "Rear sprocket teeth are required")
    @Min(1)
    @Max(FinalDriveRatio.MAX_TEETH)
    private Integer rearSprocket;

    public FinalDriveRatio toDomain() {
        return new FinalDriveRatio(frontSprocket, rearSprocket);
    }
}
