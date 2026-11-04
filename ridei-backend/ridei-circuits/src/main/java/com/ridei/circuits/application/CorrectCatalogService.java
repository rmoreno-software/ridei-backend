package com.ridei.circuits.application;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.application.port.in.UpdateLayoutUseCase;
import com.ridei.circuits.application.port.in.UpdateTrackUseCase;
import com.ridei.circuits.application.port.in.UpdateVenueUseCase;
import com.ridei.circuits.domain.exception.LayoutNotFoundException;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/** Correcciones de datos por un admin; las reglas de duplicado se vuelven a aplicar. */
public class CorrectCatalogService implements UpdateVenueUseCase, UpdateTrackUseCase, UpdateLayoutUseCase {

    private final VenueStore store;
    private final Clock clock;

    public CorrectCatalogService(VenueRepositoryPort repository, DomainEventPublisherPort eventPublisher, Clock clock) {
        this.store = new VenueStore(repository, eventPublisher);
        this.clock = clock;
    }

    @Override
    @Transactional
    public Venue update(UpdateVenueCommand command) {
        Venue venue = store.require(command.venueId());
        venue.correct(command.correction(), clock.instant());
        return store.save(venue);
    }

    @Override
    @Transactional
    public Track update(UpdateTrackCommand command) {
        Venue venue = store.require(command.venueId());
        venue.correctTrack(command.trackId(), command.correction(), clock.instant());
        return store.save(venue).track(command.trackId());
    }

    @Override
    @Transactional
    public Layout update(UpdateLayoutCommand command) {
        Venue venue = store.require(command.venueId());
        if (!venue.track(command.trackId()).hasLayout(command.layoutId())) {
            throw new LayoutNotFoundException(command.layoutId());
        }
        venue.correctLayout(command.layoutId(), command.correction(), clock.instant());
        return store.save(venue).track(command.trackId()).layout(command.layoutId());
    }
}
