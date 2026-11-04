package com.ridei.circuits.domain.model;

/** Corrección parcial de un track: cada campo {@code null} se deja como estaba. */
public record TrackCorrection(Discipline discipline, TrackName name) {

    TrackSpec applyTo(TrackSpec current) {
        return new TrackSpec(
            discipline != null ? discipline : current.discipline(),
            name != null ? name : current.name()
        );
    }
}
