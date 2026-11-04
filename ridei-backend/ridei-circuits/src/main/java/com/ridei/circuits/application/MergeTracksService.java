package com.ridei.circuits.application;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.application.port.in.MergeTracksUseCase;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/** Fusiona un track duplicado en otro, del mismo venue o de otro. Devuelve el venue destino. */
public class MergeTracksService implements MergeTracksUseCase {

    private final VenueStore store;
    private final Clock clock;

    public MergeTracksService(VenueRepositoryPort repository, DomainEventPublisherPort eventPublisher, Clock clock) {
        this.store = new VenueStore(repository, eventPublisher);
        this.clock = clock;
    }

    @Override
    @Transactional
    public Venue merge(MergeTracksCommand command) {
        Venue sourceVenue = store.requireOwnerOfTrack(command.sourceId());
        Venue targetVenue = sourceVenue.findTrack(command.targetId()).isPresent()
            ? sourceVenue
            : store.requireOwnerOfTrack(command.targetId());

        sourceVenue.mergeTrackInto(command.sourceId(), targetVenue, command.targetId(), clock.instant());

        Venue savedSource = store.save(sourceVenue);
        return targetVenue == sourceVenue ? savedSource : store.save(targetVenue);
    }
}
