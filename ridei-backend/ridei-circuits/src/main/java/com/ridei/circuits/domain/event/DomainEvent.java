package com.ridei.circuits.domain.event;

import java.time.Instant;

/** Marca de los eventos de dominio publicados por el catálogo. */
public interface DomainEvent {

    Instant occurredAt();
}
