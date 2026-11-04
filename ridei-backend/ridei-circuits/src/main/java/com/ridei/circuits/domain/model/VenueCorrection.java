package com.ridei.circuits.domain.model;

/** Corrección parcial de un venue: cada campo {@code null} se deja como estaba. */
public record VenueCorrection(
    VenueName name,
    CountryCode countryCode,
    GeoPoint location,
    ExternalPlaceId externalPlaceId
) {
}
