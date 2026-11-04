package com.ridei.circuits.application;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.application.port.in.MergeVenuesUseCase;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/**
 * Fusiona un venue duplicado en el canónico. El origen se guarda primero (ya {@code MERGED} y
 * sin tracks) y después el destino, que es quien reasigna los tracks movidos.
 */
public class MergeVenuesService implements MergeVenuesUseCase {

    private final VenueStore store;
    private final Clock clock;

    public MergeVenuesService(VenueRepositoryPort repository, DomainEventPublisherPort eventPublisher, Clock clock) {
        this.store = new VenueStore(repository, eventPublisher);
        this.clock = clock;
    }

    @Override
    @Transactional
    public Venue merge(MergeVenuesCommand command) {
        Venue source = store.require(command.sourceId());
        Venue target = store.require(command.targetId());

        source.mergeInto(target, clock.instant());

        store.save(source);
        return store.save(target);
    }
}
