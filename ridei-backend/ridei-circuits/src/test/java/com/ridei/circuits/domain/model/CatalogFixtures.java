package com.ridei.circuits.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Datos de prueba compartidos por los tests de dominio y de aplicación. */
public final class CatalogFixtures {

    public static final Instant NOW = Instant.parse("2026-11-04T10:00:00Z");
    public static final UserId PROPOSER = new UserId(UUID.fromString("00000000-0000-4000-8000-000000000001"));
    public static final GeoPoint MONTMELO = GeoPoint.of(41.5700, 2.2611);
    public static final GeoPoint ALCARRAS = GeoPoint.of(41.5530, 0.5170);

    private CatalogFixtures() {
    }

    public static TrackSpec track(Discipline discipline, String name) {
        return new TrackSpec(discipline, TrackName.of(name));
    }

    public static LayoutSpec layout(String name, int meters, Integer turns, Direction direction) {
        return new LayoutSpec(LayoutName.of(name), Length.ofMeters(meters), Turns.ofNullable(turns), direction);
    }

    public static Venue venue(String name, GeoPoint location, TrackSpec firstTrack, LayoutSpec firstLayout) {
        return Venue.propose(VenueName.of(name), CountryCode.of("ES"), location, null, PROPOSER, firstTrack, firstLayout, NOW);
    }

    public static Venue venue(String name, GeoPoint location, TrackSpec firstTrack) {
        return venue(name, location, firstTrack, null);
    }

    /** Montmeló con su track de velocidad y el trazado GP (datos ficticios para tests). */
    public static Venue montmelo() {
        return venue("Circuit de Barcelona-Catalunya", MONTMELO,
            track(Discipline.ROAD_RACING, "Velocidad"),
            layout("GP", 4657, 14, Direction.CLOCKWISE));
    }

    public static Venue alcarras() {
        return venue("Circuit d'Alcarràs", ALCARRAS, track(Discipline.ROAD_RACING, "Velocidad"));
    }

    public static Track onlyTrack(Venue venue) {
        return venue.activeTracks().get(0);
    }
}
