package com.ridei.circuits.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.Layout;

public record LayoutResponseDTO(
    UUID id,
    String name,
    /** {@code null} mientras el layout está pendiente de su primera sesión. */
    Integer lengthMeters,
    Integer turns,
    Direction direction,
    @JsonProperty("isDefault") boolean isDefault,
    CatalogStatus status,
    /**
     * Dibujo del trazado en orden de recorrido, sin repetir el primer punto al final; 3–200
     * puntos. Si hay {@code finishLine}, empieza en la meta. {@code null} si aún no tiene dibujo.
     */
    List<PathPointDTO> path,
    /**
     * Línea de meta: dos puntos, el de la izquierda y el de la derecha mirando en el sentido de
     * la marcha. {@code null} si no se conoce.
     */
    List<PathPointDTO> finishLine
) {
    public static LayoutResponseDTO fromDomain(Layout layout) {
        return new LayoutResponseDTO(
            layout.getId().value(),
            layout.getName().value(),
            layout.getLength() != null ? layout.getLength().meters() : null,
            layout.getTurns() != null ? layout.getTurns().count() : null,
            layout.getDirection(),
            layout.isDefault(),
            layout.getStatus(),
            layout.getPath() != null
                ? layout.getPath().points().stream().map(PathPointDTO::fromDomain).toList()
                : null,
            layout.getFinishLine() != null
                ? List.of(PathPointDTO.fromDomain(layout.getFinishLine().left()), PathPointDTO.fromDomain(layout.getFinishLine().right()))
                : null
        );
    }
}
