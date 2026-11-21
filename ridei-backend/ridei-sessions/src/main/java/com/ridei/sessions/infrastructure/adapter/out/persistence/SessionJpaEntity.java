package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Campos de paquete: solo los usa {@link SessionPersistenceAdapter}. */
@Entity
@Table(name = "riding_session")
class SessionJpaEntity {

    @Id
    UUID id;
    @Column(name = "rider_id", nullable = false)
    UUID riderId;
    @Column(nullable = false)
    String name;

    @Column(name = "venue_id", nullable = false)
    UUID venueId;
    @Column(name = "venue_name", nullable = false)
    String venueName;
    @Column(name = "track_name")
    String trackName;
    @Column(name = "layout_id", nullable = false)
    UUID layoutId;
    @Column(name = "layout_name", nullable = false)
    String layoutName;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_path", nullable = false, columnDefinition = "jsonb")
    String layoutPath;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "finish_line", nullable = false, columnDefinition = "jsonb")
    String finishLine;
    @Column(name = "sector_count", nullable = false)
    int sectorCount;

    @Column(name = "motorbike_id", nullable = false)
    UUID motorbikeId;
    @Column(name = "motorbike_name", nullable = false)
    String motorbikeName;
    @Column(name = "motorbike_category")
    String motorbikeCategory;
    @Column(name = "motorbike_category_group")
    String motorbikeCategoryGroup;

    @Column(name = "started_at", nullable = false)
    Instant startedAt;
    @Column(name = "ended_at", nullable = false)
    Instant endedAt;
    @Column(name = "temperature_celsius")
    Integer temperatureCelsius;
    @Column(name = "track_condition")
    String trackCondition;
    @Column
    String notes;

    @Column(name = "reversed_direction", nullable = false)
    boolean reversedDirection;
    @Column(name = "best_lap_ms")
    Long bestLapMs;
    @Column(name = "ideal_lap_ms")
    Long idealLapMs;
    @Column(name = "valid_lap_count", nullable = false)
    int validLapCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_lap", joinColumns = @JoinColumn(name = "session_id"))
    @OrderBy("lapNumber")
    List<LapEmbeddable> laps = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
    @Version
    Long version;
}
