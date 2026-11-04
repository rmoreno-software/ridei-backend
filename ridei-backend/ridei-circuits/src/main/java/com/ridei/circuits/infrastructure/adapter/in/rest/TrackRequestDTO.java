package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.TrackName;
import com.ridei.circuits.domain.model.TrackSpec;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrackRequestDTO(
    @NotNull(message = "Track discipline is required")
    Discipline discipline,

    @NotBlank(message = "Track name is required")
    @Size(max = 150, message = "Track name must be at most 150 characters")
    String name
) {
    public TrackSpec toSpec() {
        return new TrackSpec(discipline, TrackName.of(name));
    }
}
