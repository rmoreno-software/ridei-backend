package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.ridei.sessions.domain.model.GeoPoint;
import com.ridei.sessions.domain.model.TelemetrySample;

/**
 * Formato {@value #ENCODING}: GZIP de, por muestra, {@code long} tiempo (epoch ms),
 * {@code double} latitud y longitud y {@code float} velocidad, precisión e inclinación
 * ({@code NaN} = ausente). Unos 15 bytes por muestra comprimida frente a ~120 en JSON.
 */
final class TelemetryCodec {

    static final String ENCODING = "ridei-bin-v1";

    private TelemetryCodec() {}

    static byte[] encode(List<TelemetrySample> samples) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(bytes))) {
            out.writeInt(samples.size());
            for (TelemetrySample s : samples) {
                out.writeLong(s.timeMillis());
                out.writeDouble(s.position().latitude());
                out.writeDouble(s.position().longitude());
                out.writeFloat(orNaN(s.speedMps()));
                out.writeFloat(orNaN(s.accuracyMeters()));
                out.writeFloat(orNaN(s.leanDegrees()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    static List<TelemetrySample> decode(String encoding, byte[] data) {
        if (!ENCODING.equals(encoding)) throw new IllegalStateException("Unknown telemetry encoding " + encoding);
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(data)))) {
            int count = in.readInt();
            List<TelemetrySample> samples = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                long time = in.readLong();
                GeoPoint position = GeoPoint.of(in.readDouble(), in.readDouble());
                samples.add(new TelemetrySample(time, position, orNull(in.readFloat()), orNull(in.readFloat()), orNull(in.readFloat())));
            }
            return samples;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static float orNaN(Double value) {
        return value == null ? Float.NaN : value.floatValue();
    }

    private static Double orNull(float value) {
        return Float.isNaN(value) ? null : (double) value;
    }
}
