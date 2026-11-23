package com.ridei.sessions.application.port.in;

import com.ridei.sessions.application.RecordManualLapTimeCommand;
import com.ridei.sessions.application.RecordManualLapTimeResult;

/** Apuntar un tiempo de una tanda pasada (22B). */
public interface RecordManualLapTimeUseCase {
    RecordManualLapTimeResult record(RecordManualLapTimeCommand command);
}
