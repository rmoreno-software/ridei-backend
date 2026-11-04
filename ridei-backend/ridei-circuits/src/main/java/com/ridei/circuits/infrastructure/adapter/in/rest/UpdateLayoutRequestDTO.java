package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.LayoutCorrection;
import com.ridei.circuits.domain.model.LayoutName;
import com.ridei.circuits.domain.model.Length;
import com.ridei.circuits.domain.model.Turns;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** PATCH: los campos ausentes no se modifican. */
public record UpdateLayoutRequestDTO(
    @Size(min = 1, max = 150, message = "Layout name must be between 1 and 150 characters")
    String name,

    @Positive(message = "Layout length must be greater than 0 meters")
    Integer lengthMeters,

    @PositiveOrZero(message = "Turns must be zero or positive")
    Integer turns,

    Direction direction
) {
    public LayoutCorrection toCorrection() {
        return new LayoutCorrection(
            name != null ? LayoutName.of(name) : null,
            lengthMeters != null ? Length.ofMeters(lengthMeters) : null,
            Turns.ofNullable(turns),
            direction
        );
    }
}
