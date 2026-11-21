package com.ridei.sessions.domain.model;

import java.time.Instant;
import java.util.Optional;

import com.ridei.sessions.domain.service.LapTimer;
import com.ridei.sessions.domain.service.TimedRun;

/**
 * Sesión (tanda) grabada por un piloto en un trazado con una de sus motos. Raíz del agregado.
 *
 * <p>La telemetría bruta no forma parte del agregado en memoria (es grande y los listados no la
 * necesitan): se guarda aparte y se pasa explícitamente a {@link #record} y {@link #retime}.
 * Las vueltas siempre son el resultado de cronometrar esa telemetría sobre {@link #layout()}.
 */
public final class Session {

    public static final int MAX_NAME_LENGTH = 80;
    public static final int MAX_NOTES_LENGTH = 2000;

    private final SessionId id;
    private final RiderId riderId;
    private final MotorbikeSnapshot motorbike;
    private final Instant startedAt;
    private final Instant endedAt;
    private final Instant createdAt;
    private LayoutSnapshot layout;
    private String name;
    private Conditions conditions;
    private String notes;
    private LapTiming timing;
    private Instant updatedAt;

    private Session(
        SessionId id, RiderId riderId, LayoutSnapshot layout, MotorbikeSnapshot motorbike,
        String name, Conditions conditions, String notes, Instant startedAt, Instant endedAt,
        LapTiming timing, Instant createdAt, Instant updatedAt
    ) {
        this.id = id;
        this.riderId = riderId;
        this.layout = layout;
        this.motorbike = motorbike;
        this.name = name;
        this.conditions = conditions;
        this.notes = notes;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.timing = timing;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Sesión recién subida por el logger: se cronometra ya. */
    public static Session record(
        SessionId id, RiderId riderId, LayoutSnapshot layout, MotorbikeSnapshot motorbike,
        String name, Conditions conditions, String notes, Telemetry telemetry, Instant now
    ) {
        if (id == null || riderId == null) throw new IllegalArgumentException("Session and rider ids are required");
        if (layout == null || motorbike == null) throw new IllegalArgumentException("Layout and motorbike are required");
        if (telemetry == null) throw new IllegalArgumentException("Telemetry is required");
        LapTiming timing = LapTimer.time(telemetry, layout.geometry()).timing();
        return new Session(
            id, riderId, layout, motorbike,
            normalizedName(name, layout), conditions != null ? conditions : Conditions.UNKNOWN, normalizedNotes(notes),
            telemetry.startedAt(), telemetry.endedAt(), timing, now, now);
    }

    public static Session reconstitute(
        SessionId id, RiderId riderId, LayoutSnapshot layout, MotorbikeSnapshot motorbike,
        String name, Conditions conditions, String notes, Instant startedAt, Instant endedAt,
        LapTiming timing, Instant createdAt, Instant updatedAt
    ) {
        return new Session(id, riderId, layout, motorbike, name, conditions, notes, startedAt, endedAt, timing, createdAt, updatedAt);
    }

    // ── Comportamiento ──────────────────────────────────────────────────────────────────

    public boolean isOwnedBy(RiderId rider) {
        return riderId.equals(rider);
    }

    public void editDetails(String newName, Conditions newConditions, String newNotes, Instant now) {
        if (newName != null) this.name = normalizedName(newName, layout);
        if (newConditions != null) this.conditions = newConditions;
        if (newNotes != null) this.notes = normalizedNotes(newNotes);
        this.updatedAt = now;
    }

    /**
     * Vuelve a cronometrar con la geometría actual del trazado (p. ej. después de corregir la
     * meta en el catálogo). El trazado tiene que ser el mismo.
     */
    public void retime(LayoutSnapshot currentLayout, Telemetry telemetry, Instant now) {
        if (!currentLayout.layoutId().equals(layout.layoutId())) {
            throw new IllegalArgumentException("A session can only be retimed on its own layout");
        }
        this.layout = currentLayout;
        this.timing = LapTimer.time(telemetry, currentLayout.geometry()).timing();
        this.updatedAt = now;
    }

    /** Cronometraje completo (con trazas por vuelta) a partir de la telemetría guardada. */
    public TimedRun analyze(Telemetry telemetry) {
        return LapTimer.time(telemetry, layout.geometry());
    }

    public Optional<Lap> bestLap() {
        return timing.bestLap();
    }

    public Optional<IdealLap> idealLap() {
        return timing.idealLap();
    }

    private static String normalizedName(String name, LayoutSnapshot layout) {
        String value = name == null || name.isBlank() ? layout.layoutName() : name.trim();
        if (value.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Session name cannot be longer than " + MAX_NAME_LENGTH + " characters");
        }
        return value;
    }

    private static String normalizedNotes(String notes) {
        if (notes == null || notes.isBlank()) return null;
        String value = notes.trim();
        if (value.length() > MAX_NOTES_LENGTH) {
            throw new IllegalArgumentException("Session notes cannot be longer than " + MAX_NOTES_LENGTH + " characters");
        }
        return value;
    }

    // ── Getters ─────────────────────────────────────────────────────────────────────────

    public SessionId id() { return id; }
    public RiderId riderId() { return riderId; }
    public LayoutSnapshot layout() { return layout; }
    public MotorbikeSnapshot motorbike() { return motorbike; }
    public String name() { return name; }
    public Conditions conditions() { return conditions; }
    public String notes() { return notes; }
    public Instant startedAt() { return startedAt; }
    public Instant endedAt() { return endedAt; }
    public LapTiming timing() { return timing; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
