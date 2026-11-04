package com.ridei.circuits.application;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.application.port.in.ProposeVenueUseCase;
import com.ridei.circuits.domain.exception.PossibleDuplicateVenueException;
import com.ridei.circuits.domain.model.DeduplicationPolicy;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueCandidate;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/**
 * Deduplicación de venues:
 * <ol>
 *   <li>Mismo {@code externalPlaceId} ⇒ es el mismo venue: se devuelve el existente (canónico),
 *       aunque el usuario haya confirmado que no es un duplicado.</li>
 *   <li>Venues cercanos con nombre similar ⇒ 409 con candidatos, salvo {@code confirmedNotDuplicate}.</li>
 *   <li>En otro caso se crea.</li>
 * </ol>
 * El bloqueo de zona se toma antes de comprobar, para que dos propuestas simultáneas del mismo
 * sitio se vean la una a la otra.
 */
public class ProposeVenueService implements ProposeVenueUseCase {

    private final VenueRepositoryPort repository;
    private final VenueStore store;
    private final DeduplicationPolicy policy;
    private final Clock clock;

    public ProposeVenueService(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        DeduplicationPolicy policy,
        Clock clock
    ) {
        this.repository = repository;
        this.store = new VenueStore(repository, eventPublisher);
        this.policy = policy;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ProposeVenueResult propose(ProposeVenueCommand command) {
        repository.lockArea(command.location(), policy.venueRadius());

        Optional<Venue> samePlace = findBySamePlace(command);
        if (samePlace.isPresent()) return ProposeVenueResult.existing(store.canonical(samePlace.get()));

        if (!command.confirmedNotDuplicate()) rejectPossibleDuplicates(command);

        return ProposeVenueResult.created(store.save(newVenue(command)));
    }

    private Optional<Venue> findBySamePlace(ProposeVenueCommand command) {
        if (command.externalPlaceId() == null) return Optional.empty();
        return repository.findByExternalPlaceId(command.externalPlaceId());
    }

    private void rejectPossibleDuplicates(ProposeVenueCommand command) {
        List<VenueCandidate> candidates = repository.findPotentialDuplicates(
            command.name(), command.location(), policy.venueRadius(), policy.venueMinSimilarity());
        if (!candidates.isEmpty()) throw new PossibleDuplicateVenueException(candidates);
    }

    private Venue newVenue(ProposeVenueCommand command) {
        return Venue.propose(
            command.name(),
            command.countryCode(),
            command.location(),
            command.externalPlaceId(),
            command.proposedBy(),
            command.firstTrack(),
            command.firstLayout(),
            clock.instant()
        );
    }
}
