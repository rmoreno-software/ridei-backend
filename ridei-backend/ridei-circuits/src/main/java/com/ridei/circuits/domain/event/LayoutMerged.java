package com.ridei.circuits.domain.event;

import java.time.Instant;

import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.TrackId;

/** El layout {@code source} es un duplicado de {@code target}, que pertenece a {@code targetTrack}. */
public record LayoutMerged(
    LayoutId source,
    LayoutId target,
    TrackId targetTrack,
    Instant occurredAt
) implements DomainEvent {
}
