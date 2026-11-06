package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.LayoutName;
import com.ridei.circuits.domain.model.LayoutSpec;
import com.ridei.circuits.domain.model.Length;
import com.ridei.circuits.domain.model.Turns;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record LayoutRequestDTO(
    @NotBlank(message = "Layout name is required")
    @Size(max = 150, message = "Layout name must be at most 150 characters")
    String name,

    /** Opcional: la calcula el GPS con la primera sesión. */
    @Positive(message = "Layout length must be greater than 0 meters")
    Integer lengthMeters,

    @PositiveOrZero(message = "Turns must be zero or positive")
    Integer turns,

    /** Opcional: lo calcula el GPS con la primera sesión. */
    Direction direction
) {
    public LayoutSpec toSpec() {
        return new LayoutSpec(LayoutName.of(name), Length.ofNullable(lengthMeters), Turns.ofNullable(turns), direction);
    }
}
