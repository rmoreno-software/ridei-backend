package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.application.SearchVenuesQuery;
import com.ridei.circuits.domain.model.CountryCode;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.Distance;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.PageRequest;
import com.ridei.circuits.domain.model.VenueSearchCriteria;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Parámetros de {@code GET /venues}; todos opcionales. */
public record SearchVenuesRequestDTO(
    @Size(max = 150, message = "Search text must be at most 150 characters")
    String q,

    @Pattern(regexp = "[A-Za-z]{2}", message = "Country must be an ISO 3166-1 alpha-2 code")
    String country,

    Discipline discipline,

    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    Double lat,

    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    Double lon,

    @Positive(message = "Radius must be positive")
    @Max(value = 500, message = "Radius must be at most 500 km")
    Double radiusKm,

    @Min(value = 0, message = "Page must be zero or positive")
    Integer page,

    @Min(value = 1, message = "Size must be between 1 and 100")
    @Max(value = 100, message = "Size must be between 1 and 100")
    Integer size
) {
    static final int DEFAULT_SIZE = 20;

    @AssertTrue(message = "lat, lon and radiusKm must be given together")
    public boolean isProximityComplete() {
        return (lat == null) == (lon == null) && (lat == null) == (radiusKm == null);
    }

    public SearchVenuesQuery toQuery() {
        boolean near = lat != null;
        VenueSearchCriteria criteria = VenueSearchCriteria.of(
            q,
            country != null ? CountryCode.of(country) : null,
            discipline,
            near ? GeoPoint.of(lat, lon) : null,
            near ? Distance.searchRadiusOfKilometers(radiusKm) : null
        );
        return new SearchVenuesQuery(criteria, PageRequest.of(page != null ? page : 0, size != null ? size : DEFAULT_SIZE));
    }
}
