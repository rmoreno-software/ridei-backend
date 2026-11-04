package com.ridei.circuits.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.Track;

/** Solo incluye los layouts activos (los {@code MERGED} son detalle interno del catálogo). */
public record TrackResponseDTO(
    UUID id,
    Discipline discipline,
    String name,
    CatalogStatus status,
    List<LayoutResponseDTO> layouts
) {
    public static TrackResponseDTO fromDomain(Track track) {
        return new TrackResponseDTO(
            track.getId().value(),
            track.getDiscipline(),
            track.getName().value(),
            track.getStatus(),
            track.activeLayouts().stream().map(LayoutResponseDTO::fromDomain).toList()
        );
    }
}
