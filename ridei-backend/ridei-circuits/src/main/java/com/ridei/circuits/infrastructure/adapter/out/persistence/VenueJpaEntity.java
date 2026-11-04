package com.ridei.circuits.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.CountryCode;
import com.ridei.circuits.domain.model.ExternalPlaceId;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.UserId;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;
import com.ridei.circuits.domain.model.VenueName;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * La columna generada {@code location} no se mapea: la calcula PostgreSQL a partir de
 * latitude/longitude y solo la usan las consultas nativas.
 *
 * <p>Sin {@code orphanRemoval}: el catálogo nunca borra filas, y al fusionar venues los tracks
 * salen de la colección del origen para entrar en la del destino (se actualiza su venue_id).
 */
@Entity
@Table(name = "venue")
@Getter
@Setter
@NoArgsConstructor
public class VenueJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 150)
    private String normalizedName;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "external_place_id")
    private String externalPlaceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CatalogStatus status;

    @Column(name = "merged_into_id")
    private UUID mergedIntoId;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("createdAt ASC, id ASC")
    private List<TrackJpaEntity> tracks = new ArrayList<>();

    public static VenueJpaEntity fromDomain(Venue venue) {
        VenueJpaEntity entity = new VenueJpaEntity();
        entity.id = venue.getId().value();
        entity.name = venue.getName().value();
        entity.normalizedName = venue.getName().normalized();
        entity.countryCode = venue.getCountryCode().value();
        entity.latitude = venue.getLocation().latitude();
        entity.longitude = venue.getLocation().longitude();
        entity.externalPlaceId = venue.getExternalPlaceId() != null ? venue.getExternalPlaceId().value() : null;
        entity.status = venue.getStatus();
        entity.mergedIntoId = venue.getMergedInto() != null ? venue.getMergedInto().value() : null;
        entity.createdBy = venue.getCreatedBy() != null ? venue.getCreatedBy().value() : null;
        entity.createdAt = venue.getCreatedAt();
        entity.updatedAt = venue.getUpdatedAt();
        entity.version = venue.getVersion();
        entity.tracks = venue.getTracks().stream()
            .map(track -> TrackJpaEntity.fromDomain(track, entity))
            .collect(Collectors.toCollection(ArrayList::new));
        return entity;
    }

    public Venue toDomain() {
        return Venue.reconstitute(
            new VenueId(id),
            VenueName.of(name),
            CountryCode.of(countryCode),
            GeoPoint.of(latitude, longitude),
            ExternalPlaceId.ofNullable(externalPlaceId),
            status,
            mergedIntoId != null ? new VenueId(mergedIntoId) : null,
            createdBy != null ? new UserId(createdBy) : null,
            createdAt,
            updatedAt,
            version,
            tracks.stream().map(TrackJpaEntity::toDomain).toList()
        );
    }
}
