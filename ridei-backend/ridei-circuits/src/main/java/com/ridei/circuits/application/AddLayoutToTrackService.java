package com.ridei.circuits.application;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.application.port.in.AddLayoutToTrackUseCase;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

public class AddLayoutToTrackService implements AddLayoutToTrackUseCase {

    private final VenueStore store;
    private final Clock clock;

    public AddLayoutToTrackService(VenueRepositoryPort repository, DomainEventPublisherPort eventPublisher, Clock clock) {
        this.store = new VenueStore(repository, eventPublisher);
        this.clock = clock;
    }

    @Override
    @Transactional
    public Layout addLayout(AddLayoutToTrackCommand command) {
        Venue venue = store.require(command.venueId());
        Layout layout = venue.addLayout(command.trackId(), command.layout(), clock.instant());
        return store.save(venue).track(command.trackId()).layout(layout.getId());
    }
}
