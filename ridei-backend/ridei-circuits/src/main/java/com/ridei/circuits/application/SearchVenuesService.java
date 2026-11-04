package com.ridei.circuits.application;

import com.ridei.circuits.application.port.in.SearchVenuesUseCase;
import com.ridei.circuits.domain.model.PageResult;
import com.ridei.circuits.domain.model.VenueSearchHit;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

public class SearchVenuesService implements SearchVenuesUseCase {

    private final VenueRepositoryPort repository;

    public SearchVenuesService(VenueRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<VenueSearchHit> search(SearchVenuesQuery query) {
        return repository.search(query.criteria(), query.page());
    }
}
