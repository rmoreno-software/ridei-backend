package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class LapEmbeddable {

    @Column(name = "lap_number", nullable = false)
    int lapNumber;
    @Column(name = "started_at", nullable = false)
    Instant startedAt;
    @Column(name = "duration_ms", nullable = false)
    long durationMs;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "sector_ms", nullable = false, columnDefinition = "bigint[]")
    Long[] sectorMs;
    @Column(name = "distance_m", nullable = false)
    double distanceM;
    @Column(nullable = false)
    String validity;
}
