package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.TrackCorrection;
import com.ridei.circuits.domain.model.TrackName;

import jakarta.validation.constraints.Size;

/** PATCH: los campos ausentes no se modifican. */
public record UpdateTrackRequestDTO(
    Discipline discipline,

    @Size(min = 1, max = 150, message = "Track name must be between 1 and 150 characters")
    String name
) {
    public TrackCorrection toCorrection() {
        return new TrackCorrection(discipline, name != null ? TrackName.of(name) : null);
    }
}
