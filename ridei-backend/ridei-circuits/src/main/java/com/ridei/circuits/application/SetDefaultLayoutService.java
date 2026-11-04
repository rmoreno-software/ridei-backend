package com.ridei.circuits.application;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.application.port.in.SetDefaultLayoutUseCase;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

public class SetDefaultLayoutService implements SetDefaultLayoutUseCase {

    private final VenueStore store;
    private final Clock clock;

    public SetDefaultLayoutService(VenueRepositoryPort repository, DomainEventPublisherPort eventPublisher, Clock clock) {
        this.store = new VenueStore(repository, eventPublisher);
        this.clock = clock;
    }

    @Override
    @Transactional
    public Track setDefault(SetDefaultLayoutCommand command) {
        Venue venue = store.require(command.venueId());
        venue.setDefaultLayout(command.trackId(), command.layoutId(), clock.instant());
        return store.save(venue).track(command.trackId());
    }
}
