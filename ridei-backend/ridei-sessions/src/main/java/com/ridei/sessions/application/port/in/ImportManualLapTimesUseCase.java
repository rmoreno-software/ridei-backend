package com.ridei.sessions.application.port.in;

import java.util.List;

import com.ridei.sessions.application.RecordManualLapTimeCommand;
import com.ridei.sessions.application.RecordManualLapTimeResult;

/** Guardar de golpe los tiempos importados de las notas (22C, "Guardar 3 tiempos"): todos o ninguno. */
public interface ImportManualLapTimesUseCase {
    List<RecordManualLapTimeResult> recordAll(List<RecordManualLapTimeCommand> commands);
}
