package com.ridei.sessions.domain.model;

/**
 * Trazado del catálogo ({@code ridei-circuits}) tal como era al subir la sesión. Solo
 * referencias por id (sin FK entre bases de datos) más lo necesario para mostrar y cronometrar.
 */
public record LayoutSnapshot(
    VenueId venueId,
    String venueName,
    String trackName,
    LayoutId layoutId,
    String layoutName,
    TrackGeometry geometry
) {
    public LayoutSnapshot {
        if (venueId == null || layoutId == null) throw new IllegalArgumentException("Venue and layout ids are required");
        if (venueName == null || venueName.isBlank()) throw new IllegalArgumentException("Venue name is required");
        if (layoutName == null || layoutName.isBlank()) throw new IllegalArgumentException("Layout name is required");
        if (geometry == null) throw new IllegalArgumentException("Layout geometry is required");
    }

    /** El mismo trazado sin geometría. */
    public LayoutRef ref() {
        return new LayoutRef(venueId, venueName, trackName, layoutId, layoutName);
    }
}
