package com.ridei.circuits.application;

import com.ridei.circuits.application.port.in.FindNearbyVenuesUseCase;
import com.ridei.circuits.domain.model.PageResult;
import com.ridei.circuits.domain.model.VenueSearchHit;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

public class FindNearbyVenuesService implements FindNearbyVenuesUseCase {

    private final VenueRepositoryPort repository;

    public FindNearbyVenuesService(VenueRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<VenueSearchHit> findNearby(FindNearbyVenuesQuery query) {
        return repository.findNearby(query.location(), query.radius(), query.page());
    }
}
