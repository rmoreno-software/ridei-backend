package com.ridei.circuits.application;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.application.port.in.AddTrackToVenueUseCase;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

public class AddTrackToVenueService implements AddTrackToVenueUseCase {

    private final VenueStore store;
    private final Clock clock;

    public AddTrackToVenueService(VenueRepositoryPort repository, DomainEventPublisherPort eventPublisher, Clock clock) {
        this.store = new VenueStore(repository, eventPublisher);
        this.clock = clock;
    }

    @Override
    @Transactional
    public Track addTrack(AddTrackToVenueCommand command) {
        Venue venue = store.require(command.venueId());
        Track track = venue.addTrack(command.track(), clock.instant());
        return store.save(venue).track(track.getId());
    }
}
