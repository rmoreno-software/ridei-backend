package com.ridei.circuits.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ridei.circuits.domain.exception.CatalogItemAlreadyExistsException;
import com.ridei.circuits.domain.exception.ConcurrentCatalogModificationException;
import com.ridei.circuits.domain.model.CatalogStatus;
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

import lombok.AllArgsConstructor;

/**
 * Las lecturas son transaccionales (solo lectura) porque las colecciones son LAZY y el
 * mapeo a dominio las recorre; dentro de un caso de uso se unen a su transacción.
 */
@Component
@AllArgsConstructor
public class VenuePersistenceAdapter implements VenueRepositoryPort {

    private final VenueJpaRepository jpa;
    private final VenueNativeQueries nativeQueries;

    @Override
    public Venue save(Venue venue) {
        try {
            return jpa.saveAndFlush(VenueJpaEntity.fromDomain(venue)).toDomain();
        } catch (DataIntegrityViolationException ex) {
            throw new CatalogItemAlreadyExistsException("Venue " + venue.getId() + " conflicts with an existing catalog item", ex);
        } catch (OptimisticLockingFailureException ex) {
            throw new ConcurrentCatalogModificationException("Venue " + venue.getId() + " was modified concurrently", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venue> findById(VenueId id) {
        return jpa.findById(id.value()).map(VenueJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venue> findByTrackId(TrackId trackId) {
        return jpa.findVenueIdByTrackId(trackId.value()).flatMap(jpa::findById).map(VenueJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venue> findByLayoutId(LayoutId layoutId) {
        return jpa.findVenueIdByLayoutId(layoutId.value()).flatMap(jpa::findById).map(VenueJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venue> findByExternalPlaceId(ExternalPlaceId externalPlaceId) {
        return jpa.findByExternalPlaceId(externalPlaceId.value()).map(VenueJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueCandidate> findPotentialDuplicates(
        VenueName name,
        GeoPoint location,
        Distance radius,
        SimilarityThreshold minSimilarity
    ) {
        return nativeQueries.potentialDuplicates(name, location, radius, minSimilarity).stream()
            .map(row -> new VenueCandidate(
                new VenueId(row.id()),
                VenueName.of(row.name()),
                CatalogStatus.valueOf(row.status()),
                Distance.ofMeters(row.distanceMeters()),
                row.similarity()))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<VenueSearchHit> search(VenueSearchCriteria criteria, PageRequest page) {
        return toHits(nativeQueries.search(criteria, page), page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<VenueSearchHit> findNearby(GeoPoint location, Distance radius, PageRequest page) {
        return toHits(nativeQueries.nearby(location, radius, page), page);
    }

    @Override
    public void lockArea(GeoPoint location, Distance radius) {
        nativeQueries.lockArea(location, radius);
    }

    /** Carga los agregados de la página en una sola consulta y conserva el orden de la nativa. */
    private PageResult<VenueSearchHit> toHits(VenueNativeQueries.HitPage hits, PageRequest page) {
        List<UUID> ids = hits.rows().stream().map(VenueNativeQueries.HitRow::id).toList();
        Map<UUID, VenueJpaEntity> venues = jpa.findAllById(ids).stream()
            .collect(Collectors.toMap(VenueJpaEntity::getId, Function.identity()));
        List<VenueSearchHit> content = hits.rows().stream()
            .filter(row -> venues.containsKey(row.id()))
            .map(row -> new VenueSearchHit(
                venues.get(row.id()).toDomain(),
                row.distanceMeters() != null ? Distance.ofMeters(row.distanceMeters()) : null,
                row.similarity()))
            .toList();
        return new PageResult<>(content, page.page(), page.size(), hits.total());
    }
}
