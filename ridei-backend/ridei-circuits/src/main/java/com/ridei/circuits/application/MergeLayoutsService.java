package com.ridei.circuits.application;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.application.port.in.MergeLayoutsUseCase;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/** Fusiona dos layouts del mismo track. */
public class MergeLayoutsService implements MergeLayoutsUseCase {

    private final VenueStore store;
    private final Clock clock;

    public MergeLayoutsService(VenueRepositoryPort repository, DomainEventPublisherPort eventPublisher, Clock clock) {
        this.store = new VenueStore(repository, eventPublisher);
        this.clock = clock;
    }

    @Override
    @Transactional
    public Venue merge(MergeLayoutsCommand command) {
        Venue venue = store.requireOwnerOfLayout(command.sourceId());
        venue.mergeLayouts(command.sourceId(), command.targetId(), clock.instant());
        return store.save(venue);
    }
}
