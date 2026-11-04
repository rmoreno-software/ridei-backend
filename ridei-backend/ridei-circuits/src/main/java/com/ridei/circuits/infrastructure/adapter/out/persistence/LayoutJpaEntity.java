package com.ridei.circuits.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.LayoutName;
import com.ridei.circuits.domain.model.Length;
import com.ridei.circuits.domain.model.Turns;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "layout")
@Getter
@Setter
@NoArgsConstructor
public class LayoutJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private TrackJpaEntity track;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 150)
    private String normalizedName;

    @Column(name = "length_m", nullable = false)
    private int lengthMeters;

    @Column
    private Integer turns;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Direction direction;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CatalogStatus status;

    @Column(name = "merged_into_id")
    private UUID mergedIntoId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    static LayoutJpaEntity fromDomain(Layout layout, TrackJpaEntity track) {
        LayoutJpaEntity entity = new LayoutJpaEntity();
        entity.id = layout.getId().value();
        entity.track = track;
        entity.name = layout.getName().value();
        entity.normalizedName = layout.getName().normalized();
        entity.lengthMeters = layout.getLength().meters();
        entity.turns = layout.getTurns() != null ? layout.getTurns().count() : null;
        entity.direction = layout.getDirection();
        entity.isDefault = layout.isDefault();
        entity.status = layout.getStatus();
        entity.mergedIntoId = layout.getMergedInto() != null ? layout.getMergedInto().value() : null;
        entity.createdAt = layout.getCreatedAt();
        return entity;
    }

    Layout toDomain() {
        return Layout.reconstitute(
            new LayoutId(id),
            LayoutName.of(name),
            Length.ofMeters(lengthMeters),
            Turns.ofNullable(turns),
            direction,
            isDefault,
            status,
            mergedIntoId != null ? new LayoutId(mergedIntoId) : null,
            createdAt
        );
    }
}
