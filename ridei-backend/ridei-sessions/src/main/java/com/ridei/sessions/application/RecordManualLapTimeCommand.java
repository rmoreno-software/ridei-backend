package com.ridei.sessions.application;

import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.ManualLapEntry;
import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.VenueId;

/** @param id generado por la app: reintentar el guardado no duplica el tiempo */
public record RecordManualLapTimeCommand(
    ManualLapTimeId id,
    RiderId riderId,
    VenueId venueId,
    LayoutId layoutId,
    MotorbikeId motorbikeId,
    ManualLapEntry entry
) {}
