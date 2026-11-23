package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.util.List;

import com.ridei.sessions.application.RecordManualLapTimeCommand;
import com.ridei.sessions.domain.model.LapTimePrecision;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.ManualLapEntry;
import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.TimeSource;
import com.ridei.sessions.domain.model.TrackCondition;
import com.ridei.sessions.domain.model.VenueId;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Un tiempo apuntado a mano (22B). Tiempos en milisegundos.
 *
 * @param id        solo en la importación por lotes; en {@code PUT /manual-times/{id}} manda la ruta
 * @param precision opcional: si falta, la menos precisa compatible con el tiempo (105300 → décimas)
 */
public record ManualLapTimeRequestDTO(
    String id,
    @NotBlank(message = "Venue id is required") String venueId,
    @NotBlank(message = "Layout id is required") String layoutId,
    @NotBlank(message = "Motorbike id is required") String motorbikeId,
    @NotNull(message = "Date is required") LocalDate date,
    @NotNull(message = "Best lap is required") Long bestLapMillis,
    LapTimePrecision precision,
    @NotNull(message = "Time source is required") TimeSource source,
    Integer lapCount,
    TrackCondition trackCondition,
    @Size(max = ManualLapEntry.MAX_TYRES_LENGTH, message = "Tyres must be at most 100 characters") String tyres,
    @Size(max = ManualLapEntry.MAX_OTHER_LAPS, message = "At most 100 other laps") List<Long> otherLapsMillis,
    @Size(max = ManualLapEntry.MAX_NOTES_LENGTH, message = "Notes must be at most 2000 characters") String notes
) {
    public RecordManualLapTimeCommand toCommand(ManualLapTimeId timeId, RiderId rider) {
        return new RecordManualLapTimeCommand(
            timeId, rider, VenueId.of(venueId), LayoutId.of(layoutId), MotorbikeId.of(motorbikeId),
            new ManualLapEntry(date, bestLapMillis, precision, source, lapCount, trackCondition, tyres, otherLapsMillis, notes));
    }

    /** Para la importación por lotes, donde cada tiempo trae su id. */
    public RecordManualLapTimeCommand toCommand(RiderId rider) {
        if (id == null) throw new IllegalArgumentException("Each lap time needs an id");
        return toCommand(ManualLapTimeId.of(id), rider);
    }
}
