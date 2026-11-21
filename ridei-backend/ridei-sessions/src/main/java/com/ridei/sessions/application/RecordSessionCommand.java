package com.ridei.sessions.application;

import java.util.List;

import com.ridei.sessions.domain.model.Conditions;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.model.TelemetrySample;
import com.ridei.sessions.domain.model.VenueId;

/** @param sessionId generado por la app al empezar a grabar: reintentar la subida no duplica la sesión */
public record RecordSessionCommand(
    SessionId sessionId,
    RiderId riderId,
    VenueId venueId,
    LayoutId layoutId,
    MotorbikeId motorbikeId,
    String name,
    Conditions conditions,
    String notes,
    List<TelemetrySample> samples
) {}
