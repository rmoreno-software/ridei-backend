package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.SearchVenuesQuery;
import com.ridei.circuits.domain.model.PageResult;
import com.ridei.circuits.domain.model.VenueSearchHit;

public interface SearchVenuesUseCase {

    PageResult<VenueSearchHit> search(SearchVenuesQuery query);
}
