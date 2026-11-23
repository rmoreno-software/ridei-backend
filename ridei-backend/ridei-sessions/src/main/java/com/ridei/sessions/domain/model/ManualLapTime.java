package com.ridei.sessions.domain.model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Tiempo apuntado a mano de una tanda anterior a Ridei (o sin el móvil). Igual que una sesión,
 * siempre va a un trazado y a una moto, pero no tiene GPS: cuenta para el historial, la evolución y
 * el mejor tiempo manual, nunca para la vuelta ideal, los sectores ni la vuelta de referencia del
 * HUD. Inmutable: para corregirlo se borra y se apunta de nuevo.
 */
public final class ManualLapTime {

    private final ManualLapTimeId id;
    private final RiderId riderId;
    private final LayoutRef layout;
    private final MotorbikeSnapshot motorbike;
    private final ManualLapEntry entry;
    private final Instant createdAt;

    private ManualLapTime(ManualLapTimeId id, RiderId riderId, LayoutRef layout, MotorbikeSnapshot motorbike, ManualLapEntry entry, Instant createdAt) {
        if (id == null) throw new IllegalArgumentException("Manual lap time id is required");
        if (riderId == null) throw new IllegalArgumentException("Rider is required");
        if (layout == null) throw new IllegalArgumentException("Layout is required");
        if (motorbike == null) throw new IllegalArgumentException("Motorbike is required");
        if (entry == null) throw new IllegalArgumentException("Lap time is required");
        if (createdAt == null) throw new IllegalArgumentException("Creation instant is required");
        this.id = id;
        this.riderId = riderId;
        this.layout = layout;
        this.motorbike = motorbike;
        this.entry = entry;
        this.createdAt = createdAt;
    }

    /**
     * @param today hoy en UTC. No se apuntan tiempos del futuro, con un día de margen porque no
     *              sabemos la zona del piloto (en España, de 00:00 a 02:00 aún es "ayer" en UTC)
     */
    public static ManualLapTime record(
        ManualLapTimeId id, RiderId riderId, LayoutRef layout, MotorbikeSnapshot motorbike,
        ManualLapEntry entry, LocalDate today, Instant now
    ) {
        if (entry != null && today != null && entry.date().isAfter(today.plusDays(1))) {
            throw new IllegalArgumentException("Date cannot be in the future");
        }
        return new ManualLapTime(id, riderId, layout, motorbike, entry, now);
    }

    public static ManualLapTime reconstitute(
        ManualLapTimeId id, RiderId riderId, LayoutRef layout, MotorbikeSnapshot motorbike, ManualLapEntry entry, Instant createdAt
    ) {
        return new ManualLapTime(id, riderId, layout, motorbike, entry, createdAt);
    }

    public boolean isOwnedBy(RiderId rider) {
        return riderId.equals(rider);
    }

    public ManualLapTimeId id() { return id; }
    public RiderId riderId() { return riderId; }
    public LayoutRef layout() { return layout; }
    public MotorbikeSnapshot motorbike() { return motorbike; }
    public ManualLapEntry entry() { return entry; }
    public LocalDate date() { return entry.date(); }
    public long bestLapMillis() { return entry.bestLapMillis(); }
    public Instant createdAt() { return createdAt; }
}
