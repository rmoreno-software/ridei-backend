package com.ridei.circuits.application;

import java.util.List;

import com.ridei.circuits.domain.event.DomainEvent;
import com.ridei.circuits.domain.exception.LayoutNotFoundException;
import com.ridei.circuits.domain.exception.TrackNotFoundException;
import com.ridei.circuits.domain.exception.VenueNotFoundException;
import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/**
 * Acceso a agregados compartido por los casos de uso: cargar (o 404), resolver el canónico
 * de un venue fusionado, y guardar publicando después los eventos registrados.
 */
final class VenueStore {

    /** Protección frente a ciclos en {@code mergedInto}; una cadena real nunca debería ser tan larga. */
    static final int MAX_MERGE_HOPS = 10;

    private final VenueRepositoryPort repository;
    private final DomainEventPublisherPort eventPublisher;

    VenueStore(VenueRepositoryPort repository, DomainEventPublisherPort eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    Venue require(VenueId id) {
        return repository.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
    }

    Venue requireOwnerOfTrack(TrackId trackId) {
        return repository.findByTrackId(trackId).orElseThrow(() -> new TrackNotFoundException(trackId));
    }

    Venue requireOwnerOfLayout(LayoutId layoutId) {
        return repository.findByLayoutId(layoutId).orElseThrow(() -> new LayoutNotFoundException(layoutId));
    }

    /** Sigue {@code mergedInto} hasta el venue activo. */
    Venue canonical(Venue venue) {
        Venue current = venue;
        for (int hop = 0; !current.isActive(); hop++) {
            if (hop == MAX_MERGE_HOPS) throw new IllegalStateException("Merge chain too long from venue " + venue.getId());
            current = require(current.getMergedInto());
        }
        return current;
    }

    /** Guarda el agregado y, si lo acepta la base de datos, publica sus eventos pendientes. */
    Venue save(Venue venue) {
        List<DomainEvent> events = venue.pullDomainEvents();
        Venue saved = repository.save(venue);
        eventPublisher.publishAll(events);
        return saved;
    }
}
