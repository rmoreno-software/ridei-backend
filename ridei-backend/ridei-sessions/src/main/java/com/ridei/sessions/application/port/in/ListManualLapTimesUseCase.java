package com.ridei.sessions.application.port.in;

import java.util.List;

import com.ridei.sessions.application.ListManualLapTimesQuery;
import com.ridei.sessions.domain.model.ManualLapTime;

/** "Antes de Ridei" en Mis sesiones (22E) e historial de la ficha de trazado (22D). */
public interface ListManualLapTimesUseCase {
    List<ManualLapTime> list(ListManualLapTimesQuery query);
}
