package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "session_telemetry")
class TelemetryJpaEntity {

    @Id
    @Column(name = "session_id")
    UUID sessionId;
    @Column(nullable = false)
    String encoding;
    @Column(name = "sample_count", nullable = false)
    int sampleCount;
    @Column(nullable = false)
    byte[] data;
}
