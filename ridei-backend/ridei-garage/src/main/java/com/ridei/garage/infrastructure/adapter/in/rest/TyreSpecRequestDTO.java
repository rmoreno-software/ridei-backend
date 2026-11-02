package com.ridei.garage.infrastructure.adapter.in.rest;

import com.ridei.garage.domain.model.TyreSpec;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TyreSpecRequestDTO {

    @NotBlank(message = "Tyre model is required")
    @Size(max = TyreSpec.MAX_MODEL_LENGTH)
    private String model;

    @Size(max = TyreSpec.MAX_SIZE_LENGTH)
    private String size;

    public TyreSpec toDomain() {
        return new TyreSpec(model, size);
    }
}
