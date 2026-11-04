package com.ridei.circuits.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.TrackName;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "track")
@Getter
@Setter
@NoArgsConstructor
public class TrackJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private VenueJpaEntity venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Discipline discipline;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 150)
    private String normalizedName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CatalogStatus status;

    @Column(name = "merged_into_id")
    private UUID mergedIntoId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("createdAt ASC, id ASC")
    private List<LayoutJpaEntity> layouts = new ArrayList<>();

    static TrackJpaEntity fromDomain(Track track, VenueJpaEntity venue) {
        TrackJpaEntity entity = new TrackJpaEntity();
        entity.id = track.getId().value();
        entity.venue = venue;
        entity.discipline = track.getDiscipline();
        entity.name = track.getName().value();
        entity.normalizedName = track.getName().normalized();
        entity.status = track.getStatus();
        entity.mergedIntoId = track.getMergedInto() != null ? track.getMergedInto().value() : null;
        entity.createdAt = track.getCreatedAt();
        entity.layouts = track.getLayouts().stream()
            .map(layout -> LayoutJpaEntity.fromDomain(layout, entity))
            .collect(Collectors.toCollection(ArrayList::new));
        return entity;
    }

    Track toDomain() {
        return Track.reconstitute(
            new TrackId(id),
            discipline,
            TrackName.of(name),
            status,
            mergedIntoId != null ? new TrackId(mergedIntoId) : null,
            createdAt,
            layouts.stream().map(LayoutJpaEntity::toDomain).toList()
        );
    }
}
