package com.ridei.sessions.application.port.in;

import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.RiderId;

public interface DeleteManualLapTimeUseCase {
    void delete(RiderId riderId, ManualLapTimeId id);
}
