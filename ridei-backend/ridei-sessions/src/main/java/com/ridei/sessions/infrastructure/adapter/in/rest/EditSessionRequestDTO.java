package com.ridei.sessions.infrastructure.adapter.in.rest;

import com.ridei.sessions.application.EditSessionCommand;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.SessionId;

import jakarta.validation.constraints.Size;

/** Campos ausentes = sin cambios. */
public record EditSessionRequestDTO(
    @Size(max = 80, message = "name cannot be longer than 80 characters") String name,
    ConditionsDTO conditions,
    @Size(max = 2000, message = "notes cannot be longer than 2000 characters") String notes
) {
    public EditSessionCommand toCommand(RiderId riderId, SessionId sessionId) {
        return new EditSessionCommand(riderId, sessionId, name, conditions != null ? conditions.toDomain() : null, notes);
    }
}
