package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;

/** Venue canónico para el id pedido; si el id estaba fusionado, {@code venue} es su destino. */
public record VenueDetail(VenueId requestedId, Venue venue) {

    public boolean redirectedFromMerged() {
        return !requestedId.equals(venue.getId());
    }
}
