package com.ridei.circuits.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.ridei.circuits.domain.model.Distance;
import com.ridei.circuits.domain.model.ExternalPlaceId;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.PageRequest;
import com.ridei.circuits.domain.model.PageResult;
import com.ridei.circuits.domain.model.SimilarityThreshold;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueCandidate;
import com.ridei.circuits.domain.model.VenueId;
import com.ridei.circuits.domain.model.VenueName;
import com.ridei.circuits.domain.model.VenueSearchCriteria;
import com.ridei.circuits.domain.model.VenueSearchHit;

public interface VenueRepositoryPort {

    /**
     * Persiste el agregado completo y devuelve el estado guardado (con su nueva versión).
     *
     * @throws com.ridei.circuits.domain.exception.CatalogItemAlreadyExistsException si una restricción UNIQUE lo rechaza
     * @throws com.ridei.circuits.domain.exception.ConcurrentCatalogModificationException si la versión optimista no coincide
     */
    Venue save(Venue venue);

    Optional<Venue> findById(VenueId id);

    Optional<Venue> findByTrackId(TrackId trackId);

    Optional<Venue> findByLayoutId(LayoutId layoutId);

    /** Incluye venues {@code MERGED}: quien llama decide si seguir {@code mergedInto}. */
    Optional<Venue> findByExternalPlaceId(ExternalPlaceId externalPlaceId);

    /** Venues activos a menos de {@code radius} con similitud de nombre ≥ {@code minSimilarity}. */
    List<VenueCandidate> findPotentialDuplicates(
        VenueName name,
        GeoPoint location,
        Distance radius,
        SimilarityThreshold minSimilarity
    );

    /** Excluye {@code MERGED}. */
    PageResult<VenueSearchHit> search(VenueSearchCriteria criteria, PageRequest page);

    /** Venues activos a menos de {@code radius}, del más cercano al más lejano. */
    PageResult<VenueSearchHit> findNearby(GeoPoint location, Distance radius, PageRequest page);

    /**
     * Serializa, hasta el final de la transacción, las propuestas de venue en la zona de
     * {@code radius} alrededor de {@code location}, para que dos propuestas simultáneas del
     * mismo sitio no pasen ambas la comprobación de duplicados.
     */
    void lockArea(GeoPoint location, Distance radius);
}
