package com.ridei.circuits.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ridei.circuits.domain.model.DeduplicationPolicy;
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
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/**
 * Doble de prueba del repositorio. La detección de duplicados reproduce en memoria la regla
 * de la consulta nativa (distancia + similitud de trigramas) usando las funciones del dominio.
 */
class InMemoryVenueRepository implements VenueRepositoryPort {

    private final Map<VenueId, Venue> venues = new LinkedHashMap<>();
    final List<VenueId> saves = new ArrayList<>();
    final List<GeoPoint> lockedAreas = new ArrayList<>();

    Venue given(Venue venue) {
        venue.pullDomainEvents();
        venues.put(venue.getId(), venue);
        return venue;
    }

    @Override
    public Venue save(Venue venue) {
        saves.add(venue.getId());
        venues.put(venue.getId(), venue);
        return venue;
    }

    @Override
    public Optional<Venue> findById(VenueId id) {
        return Optional.ofNullable(venues.get(id));
    }

    @Override
    public Optional<Venue> findByTrackId(TrackId trackId) {
        return venues.values().stream().filter(venue -> venue.findTrack(trackId).isPresent()).findFirst();
    }

    @Override
    public Optional<Venue> findByLayoutId(LayoutId layoutId) {
        return venues.values().stream()
            .filter(venue -> venue.getTracks().stream().anyMatch(track -> track.hasLayout(layoutId)))
            .findFirst();
    }

    @Override
    public Optional<Venue> findByExternalPlaceId(ExternalPlaceId externalPlaceId) {
        return venues.values().stream().filter(venue -> externalPlaceId.equals(venue.getExternalPlaceId())).findFirst();
    }

    @Override
    public List<VenueCandidate> findPotentialDuplicates(
        VenueName name,
        GeoPoint location,
        Distance radius,
        SimilarityThreshold minSimilarity
    ) {
        DeduplicationPolicy policy = new DeduplicationPolicy(radius, minSimilarity);
        return venues.values().stream()
            .filter(Venue::isActive)
            .map(venue -> new VenueCandidate(venue.getId(), venue.getName(), venue.getStatus(),
                venue.getLocation().distanceTo(location), venue.getName().similarityTo(name)))
            .filter(candidate -> policy.isPossibleDuplicate(candidate.distance(), candidate.similarity()))
            .sorted(Comparator.comparingDouble(VenueCandidate::similarity).reversed())
            .toList();
    }

    @Override
    public PageResult<VenueSearchHit> search(VenueSearchCriteria criteria, PageRequest page) {
        throw new UnsupportedOperationException("Covered by the PostGIS integration tests");
    }

    @Override
    public PageResult<VenueSearchHit> findNearby(GeoPoint location, Distance radius, PageRequest page) {
        throw new UnsupportedOperationException("Covered by the PostGIS integration tests");
    }

    @Override
    public void lockArea(GeoPoint location, Distance radius) {
        lockedAreas.add(location);
    }
}
