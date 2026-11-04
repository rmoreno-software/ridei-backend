package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.PageRequest;
import com.ridei.circuits.domain.model.VenueSearchCriteria;

public record SearchVenuesQuery(
    VenueSearchCriteria criteria,
    PageRequest page
) {
    public SearchVenuesQuery {
        if (criteria == null || page == null) {
            throw new IllegalArgumentException("Criteria and page are required");
        }
    }
}
