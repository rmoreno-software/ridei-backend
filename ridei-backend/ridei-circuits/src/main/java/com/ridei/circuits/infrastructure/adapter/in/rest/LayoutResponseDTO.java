package com.ridei.circuits.infrastructure.adapter.in.rest;

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
    CatalogStatus status
) {
    public static LayoutResponseDTO fromDomain(Layout layout) {
        return new LayoutResponseDTO(
            layout.getId().value(),
            layout.getName().value(),
            layout.getLength() != null ? layout.getLength().meters() : null,
            layout.getTurns() != null ? layout.getTurns().count() : null,
            layout.getDirection(),
            layout.isDefault(),
            layout.getStatus()
        );
    }
}
