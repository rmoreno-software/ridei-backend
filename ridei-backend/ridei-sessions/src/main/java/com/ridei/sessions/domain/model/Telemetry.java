package com.ridei.sessions.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Registro completo de una tanda: muestras ordenadas por tiempo, sin instantes repetidos y sin
 * las de precisión demasiado mala. Se guarda aparte de la sesión (es grande) para poder
 * recalcular las vueltas si cambia la meta.
 */
public final class Telemetry {

    public static final int MIN_SAMPLES = 10;
    public static final int MAX_SAMPLES = 250_000;
    public static final Duration MAX_DURATION = Duration.ofHours(6);
    /** Fixes con peor precisión se descartan: a 25 m el paso por meta ya es una lotería. */
    public static final double MAX_ACCURACY_METERS = 25;

    private final List<TelemetrySample> samples;

    private Telemetry(List<TelemetrySample> samples) {
        this.samples = samples;
    }

    /** Desde lo que envía el logger: ordena, quita duplicados y fixes imprecisos, y valida. */
    public static Telemetry recorded(List<TelemetrySample> raw) {
        if (raw == null) throw new IllegalArgumentException("Telemetry samples are required");
        if (raw.size() > MAX_SAMPLES) {
            throw new IllegalArgumentException("A session cannot have more than " + MAX_SAMPLES + " samples");
        }
        List<TelemetrySample> sorted = new ArrayList<>(raw);
        if (sorted.contains(null)) throw new IllegalArgumentException("Telemetry samples cannot be null");
        sorted.sort(Comparator.comparingLong(TelemetrySample::timeMillis));
        List<TelemetrySample> clean = new ArrayList<>(sorted.size());
        for (TelemetrySample sample : sorted) {
            if (sample.accuracyMeters() != null && sample.accuracyMeters() > MAX_ACCURACY_METERS) continue;
            if (!clean.isEmpty() && clean.get(clean.size() - 1).timeMillis() == sample.timeMillis()) continue;
            clean.add(sample);
        }
        if (clean.size() < MIN_SAMPLES) {
            throw new IllegalArgumentException("A session needs at least " + MIN_SAMPLES + " usable GPS samples");
        }
        Telemetry telemetry = new Telemetry(List.copyOf(clean));
        if (telemetry.duration().compareTo(MAX_DURATION) > 0) {
            throw new IllegalArgumentException("A session cannot last more than " + MAX_DURATION.toHours() + " hours");
        }
        return telemetry;
    }

    /** Ya limpia (leída de la base de datos). */
    public static Telemetry reconstitute(List<TelemetrySample> samples) {
        return new Telemetry(List.copyOf(samples));
    }

    public List<TelemetrySample> samples() {
        return samples;
    }

    public Instant startedAt() {
        return Instant.ofEpochMilli(samples.get(0).timeMillis());
    }

    public Instant endedAt() {
        return Instant.ofEpochMilli(samples.get(samples.size() - 1).timeMillis());
    }

    public Duration duration() {
        return Duration.between(startedAt(), endedAt());
    }
}
