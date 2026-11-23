package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** "Guardar 3 tiempos" (22C): cada uno con su id, generado por la app. Todos o ninguno. */
public record ImportManualLapTimesRequestDTO(
    @NotEmpty(message = "At least one lap time is required")
    @Size(max = 100, message = "At most 100 lap times at once")
    List<@Valid ManualLapTimeRequestDTO> times
) {}
