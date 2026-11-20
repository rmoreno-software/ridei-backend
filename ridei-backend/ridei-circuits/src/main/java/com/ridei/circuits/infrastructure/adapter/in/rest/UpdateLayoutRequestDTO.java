package com.ridei.circuits.infrastructure.adapter.in.rest;

import java.util.List;

import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.FinishLine;
import com.ridei.circuits.domain.model.LayoutCorrection;
import com.ridei.circuits.domain.model.LayoutName;
import com.ridei.circuits.domain.model.LayoutPath;
import com.ridei.circuits.domain.model.Length;
import com.ridei.circuits.domain.model.Turns;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

    Direction direction,

    /**
     * Dibujo nuevo del trazado, en orden de recorrido. Se simplifica (Douglas-Peucker) a 200
     * puntos como máximo; si repite el primer punto al final, se descarta.
     */
    @Size(min = LayoutPath.MIN_POINTS, max = MAX_RAW_PATH_POINTS,
        message = "Layout path must have between 3 and 20000 points")
    List<@NotNull(message = "Path points cannot be null") @Valid PathPointDTO> path,

    /**
     * Línea de meta: dos puntos, izquierda y derecha mirando en el sentido de la marcha. Tiene
     * que cortar el dibujo (el enviado o el que ya tenga) una sola vez; el dibujo pasa a
     * empezar en ella.
     */
    @Size(min = 2, max = 2, message = "Finish line must have exactly 2 points")
    List<@NotNull(message = "Finish line points cannot be null") @Valid PathPointDTO> finishLine
) {
    /** Límite de la entrada sin simplificar (una vuelta GPS a 1 Hz cabe de sobra). */
    static final int MAX_RAW_PATH_POINTS = 20_000;

    public LayoutCorrection toCorrection() {
        return new LayoutCorrection(
            name != null ? LayoutName.of(name) : null,
            lengthMeters != null ? Length.ofMeters(lengthMeters) : null,
            Turns.ofNullable(turns),
            direction,
            path != null ? LayoutPath.simplified(path.stream().map(PathPointDTO::toDomain).toList()) : null,
            finishLine != null ? FinishLine.of(finishLine.get(0).toDomain(), finishLine.get(1).toDomain()) : null
        );
    }
}
