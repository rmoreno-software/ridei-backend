package com.ridei.circuits.domain.model;

/** Datos con los que se da de alta un track. */
public record TrackSpec(Discipline discipline, TrackName name) {

    public TrackSpec {
        if (discipline == null) throw new IllegalArgumentException("Discipline is required");
        if (name == null) throw new IllegalArgumentException("Track name is required");
    }
}
