package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ridei.sessions.application.RecordSessionCommand;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.model.Telemetry;
import com.ridei.sessions.domain.model.TelemetrySample;
import com.ridei.sessions.domain.model.VenueId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Tanda grabada por la app. {@code venueId}/{@code layoutId} salen del catálogo de circuitos
 * (el trazado elegido o detectado antes de grabar) y {@code motorbikeId} del garaje.
 */
public record RecordSessionRequestDTO(
    @NotBlank(message = "venueId is required") String venueId,
    @NotBlank(message = "layoutId is required") String layoutId,
    @NotBlank(message = "motorbikeId is required") String motorbikeId,
    @Size(max = 80, message = "name cannot be longer than 80 characters") String name,
    @Valid ConditionsDTO conditions,
    @Size(max = 2000, message = "notes cannot be longer than 2000 characters") String notes,
    @NotNull(message = "samples are required")
    @Size(min = Telemetry.MIN_SAMPLES, max = Telemetry.MAX_SAMPLES, message = "samples must have between 10 and 250000 GPS fixes")
    List<SampleDTO> samples
) {
    /**
     * Una muestra del logger.
     *
     * @param t        instante del fix GPS, epoch en milisegundos
     * @param lat      latitud (grados)
     * @param lon      longitud (grados)
     * @param speed    velocidad GPS en m/s (opcional; si falta o es negativa, p. ej. -1 de iOS, se deriva de las posiciones)
     * @param accuracy precisión horizontal en metros (opcional; negativa o &gt; 25 m: se descarta el fix)
     * @param lean     inclinación en grados, + derecha / − izquierda (opcional, IMU)
     */
    public record SampleDTO(Long t, Double lat, Double lon, Double speed, Double accuracy, Double lean) {
        /** Vacío si el fix no es válido: se descarta esa muestra, no la sesión. */
        Optional<TelemetrySample> toDomain() {
            return TelemetrySample.fromLogger(t, lat, lon, speed, accuracy, lean);
        }
    }

    public RecordSessionCommand toCommand(SessionId sessionId, RiderId riderId) {
        return new RecordSessionCommand(
            sessionId,
            riderId,
            VenueId.of(venueId),
            LayoutId.of(layoutId),
            MotorbikeId.of(motorbikeId),
            name,
            conditions != null ? conditions.toDomain() : null,
            notes,
            samples.stream().filter(Objects::nonNull).map(SampleDTO::toDomain).flatMap(Optional::stream).toList());
    }
}
