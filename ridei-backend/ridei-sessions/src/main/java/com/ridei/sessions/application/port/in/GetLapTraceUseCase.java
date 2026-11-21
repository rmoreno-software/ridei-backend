package com.ridei.sessions.application.port.in;

import com.ridei.sessions.application.LapReference;
import com.ridei.sessions.domain.model.LapComparison;
import com.ridei.sessions.domain.model.LapTrace;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.SessionId;

public interface GetLapTraceUseCase {

    /** Una vuelta para el visor de trazada (posición + velocidad por distancia). */
    LapTrace trace(RiderId riderId, SessionId sessionId, int lapNumber);

    /** La vuelta ideal cosida como traza. */
    LapTrace idealTrace(RiderId riderId, SessionId sessionId);

    /** Una vuelta frente a la ideal, la mejor u otra vuelta de la misma sesión. */
    LapComparison compare(RiderId riderId, SessionId sessionId, int lapNumber, LapReference reference);
}
