package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Campos de paquete: solo los usa {@link ManualLapTimePersistenceAdapter}. */
@Entity
@Table(name = "manual_lap_time")
class ManualLapTimeJpaEntity {

    @Id
    UUID id;
    @Column(name = "rider_id", nullable = false)
    UUID riderId;

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

    @Column(name = "motorbike_id", nullable = false)
    UUID motorbikeId;
    @Column(name = "motorbike_name", nullable = false)
    String motorbikeName;
    @Column(name = "motorbike_category")
    String motorbikeCategory;
    @Column(name = "motorbike_category_group")
    String motorbikeCategoryGroup;

    @Column(name = "lap_date", nullable = false)
    LocalDate lapDate;
    @Column(name = "best_lap_ms", nullable = false)
    long bestLapMs;
    @Column(name = "lap_precision", nullable = false)
    String lapPrecision;
    @Column(name = "time_source", nullable = false)
    String timeSource;

    @Column(name = "lap_count")
    Integer lapCount;
    @Column(name = "track_condition")
    String trackCondition;
    @Column
    String tyres;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "other_laps_ms", nullable = false, columnDefinition = "bigint[]")
    Long[] otherLapsMs;
    @Column
    String notes;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}
