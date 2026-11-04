package com.ridei.circuits.domain.model;

/**
 * Resultado de búsqueda: el venue, su distancia al punto de referencia (si se dio uno) y la
 * similitud con el texto buscado (si se buscó texto).
 */
public record VenueSearchHit(Venue venue, Distance distance, Double similarity) {

    public VenueSearchHit {
        if (venue == null) throw new IllegalArgumentException("Venue is required");
    }
}
