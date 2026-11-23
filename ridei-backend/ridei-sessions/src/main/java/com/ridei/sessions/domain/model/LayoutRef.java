package com.ridei.sessions.domain.model;

/**
 * Trazado del catálogo ({@code ridei-circuits}) sin geometría: lo justo para mostrar y agrupar.
 * Los tiempos manuales lo usan porque no se cronometran, así que valen para trazados sin meta ni
 * dibujo.
 */
public record LayoutRef(
    VenueId venueId,
    String venueName,
    String trackName,
    LayoutId layoutId,
    String layoutName
) {
    public LayoutRef {
        if (venueId == null || layoutId == null) throw new IllegalArgumentException("Venue and layout ids are required");
        if (venueName == null || venueName.isBlank()) throw new IllegalArgumentException("Venue name is required");
        if (layoutName == null || layoutName.isBlank()) throw new IllegalArgumentException("Layout name is required");
    }
}
