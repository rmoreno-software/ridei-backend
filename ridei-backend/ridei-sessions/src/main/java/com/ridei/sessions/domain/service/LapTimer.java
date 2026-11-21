package com.ridei.sessions.domain.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.ridei.sessions.domain.model.Gate;
import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.LapTiming;
import com.ridei.sessions.domain.model.LapValidity;
import com.ridei.sessions.domain.model.PlanarProjection;
import com.ridei.sessions.domain.model.PlanarProjection.Vector;
import com.ridei.sessions.domain.model.Telemetry;
import com.ridei.sessions.domain.model.TelemetrySample;
import com.ridei.sessions.domain.model.TrackGeometry;

/**
 * Crono GPS: convierte una tanda (muestras) en vueltas y sectores sobre un trazado.
 *
 * <ul>
 *   <li><b>Pasos por meta interpolados</b>: un móvil suele dar 1 fix por segundo (~40 m a
 *       150 km/h); el instante del cruce se interpola dentro del tramo entre dos muestras.</li>
 *   <li><b>Meta alargada por la izquierda</b> ({@value #FINISH_LEFT_EXTENSION_METERS} m): la
 *       puerta del catálogo acaba en el muro de boxes y un GPS con error lateral podría pasar
 *       por fuera. Por la derecha no, para que el pit lane no cuente como vuelta.</li>
 *   <li><b>Sentido</b>: solo cuentan los cruces en un sentido. Si la mayoría van al revés que
 *       el catálogo (el sentido de algunos trazados es inferido), se cronometra al revés y se
 *       marca {@link LapTiming#reversedDirection()} en vez de devolver cero vueltas.</li>
 *   <li><b>Vuelta de salida y de entrada</b>: lo anterior al primer paso por meta y lo
 *       posterior al último no son vueltas.</li>
 *   <li><b>Validez</b>: el recorrido de cada vuelta se compara con la longitud del trazado; una
 *       meta no detectada (dos vueltas en una) o un paso por boxes salen como no válidas.</li>
 * </ul>
 */
public final class LapTimer {

    public static final double FINISH_LEFT_EXTENSION_METERS = 15;
    public static final double MIN_LAP_LENGTH_RATIO = 0.85;
    public static final double MAX_LAP_LENGTH_RATIO = 1.15;
    /** Un hueco mayor entre fixes dentro de una vuelta la invalida. */
    public static final long MAX_SAMPLE_GAP_MILLIS = 5_000;
    /** Cruces de meta más cercanos que esta fracción del trazado son rebotes del GPS parado en la línea. */
    private static final double MIN_DISTANCE_BETWEEN_CROSSINGS_RATIO = 0.3;
    /** Una puerta de sector solo cuenta cerca de donde toca dentro de la vuelta (fracción del trazado). */
    private static final double SECTOR_GATE_TOLERANCE_RATIO = 0.15;

    private LapTimer() {}

    public static TimedRun time(Telemetry telemetry, TrackGeometry catalogGeometry) {
        Track forward = new Track(telemetry, catalogGeometry);
        List<Event> forwardCrossings = forward.finishCrossings(true);
        List<Event> backwardCrossings = forward.finishCrossings(false);
        if (backwardCrossings.size() > forwardCrossings.size()) {
            Track reversed = new Track(telemetry, catalogGeometry.reversed());
            return reversed.timed(reversed.finishCrossings(true), true);
        }
        return forward.timed(forwardCrossings, false);
    }

    /** Paso por una puerta: instante interpolado y distancia recorrida desde el inicio de la tanda. */
    record Event(int segment, double timeMillis, double traveledMeters) {}

    /** Las muestras proyectadas sobre el plano de un trazado concreto (en un sentido). */
    static final class Track {

        final TrackGeometry geometry;
        final List<TelemetrySample> samples;
        final Vector[] plane;
        final double[] traveled;
        final double length;

        Track(Telemetry telemetry, TrackGeometry geometry) {
            this.geometry = geometry;
            this.samples = telemetry.samples();
            this.length = geometry.lengthMeters();
            PlanarProjection projection = PlanarProjection.around(geometry.path().get(0));
            this.plane = new Vector[samples.size()];
            this.traveled = new double[samples.size()];
            for (int i = 0; i < samples.size(); i++) {
                plane[i] = projection.toPlane(samples.get(i).position());
                traveled[i] = i == 0 ? 0 : traveled[i - 1] + step(i);
            }
        }

        /**
         * Distancia recorrida entre la muestra {@code i - 1} y la {@code i}. Con velocidad GPS
         * (Doppler, mucho más precisa que la posición) se integra la velocidad; sumar distancias
         * entre fixes con unos metros de ruido infla el recorrido, sobre todo a 10–25 Hz.
         */
        private double step(int i) {
            TelemetrySample previous = samples.get(i - 1);
            TelemetrySample current = samples.get(i);
            if (previous.speedMps() != null && current.speedMps() != null) {
                double seconds = (current.timeMillis() - previous.timeMillis()) / 1000.0;
                return (previous.speedMps() + current.speedMps()) / 2 * seconds;
            }
            return plane[i].minus(plane[i - 1]).length();
        }

        PlanarProjection projection() {
            return PlanarProjection.around(geometry.path().get(0));
        }

        List<Event> finishCrossings(boolean forward) {
            Gate.OnPlane finish = geometry.finishLine().extendedToTheLeft(FINISH_LEFT_EXTENSION_METERS).onPlane(projection());
            List<Event> crossings = new ArrayList<>();
            for (int i = 0; i + 1 < plane.length; i++) {
                Gate.Crossing crossing = finish.crossing(plane[i], plane[i + 1]);
                if (crossing == null || crossing.forward() != forward) continue;
                Event event = eventAt(i, crossing.fraction());
                if (!crossings.isEmpty()
                    && event.traveledMeters() - crossings.get(crossings.size() - 1).traveledMeters()
                        < MIN_DISTANCE_BETWEEN_CROSSINGS_RATIO * length) {
                    continue;
                }
                crossings.add(event);
            }
            return crossings;
        }

        Event eventAt(int segment, double fraction) {
            long t0 = samples.get(segment).timeMillis();
            long t1 = samples.get(segment + 1).timeMillis();
            return new Event(
                segment,
                t0 + fraction * (t1 - t0),
                traveled[segment] + fraction * (traveled[segment + 1] - traveled[segment]));
        }

        TimedRun timed(List<Event> finishCrossings, boolean reversed) {
            int sectorCount = geometry.sectorCount();
            List<Gate.OnPlane> sectorGates = new ArrayList<>();
            for (int k = 1; k < sectorCount; k++) sectorGates.add(geometry.sectorGate(k).onPlane(projection()));

            List<Lap> laps = new ArrayList<>();
            List<Event[]> boundaries = new ArrayList<>();
            for (int i = 0; i + 1 < finishCrossings.size(); i++) {
                Event start = finishCrossings.get(i);
                Event end = finishCrossings.get(i + 1);
                Event[] lapBoundaries = new Event[sectorCount + 1];
                lapBoundaries[0] = start;
                lapBoundaries[sectorCount] = end;
                for (int k = 1; k < sectorCount; k++) {
                    lapBoundaries[k] = sectorCrossing(sectorGates.get(k - 1), start, end, (double) k / sectorCount);
                }
                laps.add(lap(laps.size() + 1, lapBoundaries));
                boundaries.add(lapBoundaries);
            }
            return new TimedRun(this, new LapTiming(laps, sectorCount, reversed), boundaries);
        }

        private Event sectorCrossing(Gate.OnPlane gate, Event lapStart, Event lapEnd, double expectedFraction) {
            for (int s = lapStart.segment(); s <= lapEnd.segment() && s + 1 < plane.length; s++) {
                Gate.Crossing crossing = gate.crossing(plane[s], plane[s + 1]);
                if (crossing == null || !crossing.forward()) continue;
                Event event = eventAt(s, crossing.fraction());
                if (event.timeMillis() <= lapStart.timeMillis() || event.timeMillis() >= lapEnd.timeMillis()) continue;
                double fraction = (event.traveledMeters() - lapStart.traveledMeters()) / length;
                if (Math.abs(fraction - expectedFraction) <= SECTOR_GATE_TOLERANCE_RATIO) return event;
            }
            return null;
        }

        private Lap lap(int number, Event[] boundaries) {
            Event start = boundaries[0];
            Event end = boundaries[boundaries.length - 1];
            List<Long> sectors = new ArrayList<>();
            for (int k = 1; k < boundaries.length; k++) {
                Event from = boundaries[k - 1];
                Event to = boundaries[k];
                // Se redondean los instantes, no las diferencias: así los sectores suman la vuelta exacta.
                boolean timed = from != null && to != null && Math.round(to.timeMillis()) > Math.round(from.timeMillis());
                sectors.add(timed ? Math.round(to.timeMillis()) - Math.round(from.timeMillis()) : null);
            }
            double distance = end.traveledMeters() - start.traveledMeters();
            return new Lap(
                number,
                Instant.ofEpochMilli(Math.round(start.timeMillis())),
                Math.round(end.timeMillis()) - Math.round(start.timeMillis()),
                sectors,
                distance,
                validity(distance, start, end));
        }

        private LapValidity validity(double distance, Event start, Event end) {
            double ratio = distance / length;
            if (ratio < MIN_LAP_LENGTH_RATIO) return LapValidity.TOO_SHORT;
            if (ratio > MAX_LAP_LENGTH_RATIO) return LapValidity.TOO_LONG;
            for (int s = start.segment(); s <= end.segment() && s + 1 < samples.size(); s++) {
                if (samples.get(s + 1).timeMillis() - samples.get(s).timeMillis() > MAX_SAMPLE_GAP_MILLIS) {
                    return LapValidity.GPS_GAP;
                }
            }
            return LapValidity.VALID;
        }
    }
}
