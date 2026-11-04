package com.ridei.circuits.application;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.application.port.in.VerifyLayoutUseCase;
import com.ridei.circuits.application.port.in.VerifyTrackUseCase;
import com.ridei.circuits.application.port.in.VerifyVenueUseCase;
import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/** Un admin marca como {@code VERIFIED} un venue, un track o un layout. */
public class VerifyCatalogService implements VerifyVenueUseCase, VerifyTrackUseCase, VerifyLayoutUseCase {

    private final VenueStore store;
    private final Clock clock;

    public VerifyCatalogService(VenueRepositoryPort repository, DomainEventPublisherPort eventPublisher, Clock clock) {
        this.store = new VenueStore(repository, eventPublisher);
        this.clock = clock;
    }

    @Override
    @Transactional
    public Venue verify(VenueId venueId) {
        Venue venue = store.require(venueId);
        venue.verify(clock.instant());
        return store.save(venue);
    }

    @Override
    @Transactional
    public Venue verify(TrackId trackId) {
        Venue venue = store.requireOwnerOfTrack(trackId);
        venue.verifyTrack(trackId, clock.instant());
        return store.save(venue);
    }

    @Override
    @Transactional
    public Venue verify(LayoutId layoutId) {
        Venue venue = store.requireOwnerOfLayout(layoutId);
        venue.verifyLayout(layoutId, clock.instant());
        return store.save(venue);
    }
}
