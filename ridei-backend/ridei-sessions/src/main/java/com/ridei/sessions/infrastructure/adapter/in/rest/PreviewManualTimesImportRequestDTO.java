package com.ridei.sessions.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Las notas tal cual las tiene el piloto: una tanda por línea (22C). */
public record PreviewManualTimesImportRequestDTO(
    @NotBlank(message = "Notes are required")
    @Size(max = 20_000, message = "Notes must be at most 20000 characters")
    String notes
) {}
