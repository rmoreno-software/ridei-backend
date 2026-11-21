package com.ridei.sessions.application.port.in;

import java.util.Optional;

import com.ridei.sessions.application.ReferenceLap;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;

public interface GetReferenceLapUseCase {
    /** Vacío si el piloto aún no tiene vueltas válidas con esa moto en ese trazado. */
    Optional<ReferenceLap> reference(RiderId riderId, LayoutId layoutId, MotorbikeId motorbikeId);
}
