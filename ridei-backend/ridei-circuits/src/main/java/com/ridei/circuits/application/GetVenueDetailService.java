package com.ridei.circuits.application;

import com.ridei.circuits.application.port.in.GetVenueDetailUseCase;
import com.ridei.circuits.domain.model.VenueId;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/** Si el id está {@code MERGED} devuelve el venue canónico e indica que hubo redirección. */
public class GetVenueDetailService implements GetVenueDetailUseCase {

    private static final DomainEventPublisherPort NO_EVENTS = event -> {
        throw new IllegalStateException("Queries do not publish events");
    };

    private final VenueStore store;

    public GetVenueDetailService(VenueRepositoryPort repository) {
        this.store = new VenueStore(repository, NO_EVENTS);
    }

    @Override
    public VenueDetail get(VenueId venueId) {
        return new VenueDetail(venueId, store.canonical(store.require(venueId)));
    }
}
