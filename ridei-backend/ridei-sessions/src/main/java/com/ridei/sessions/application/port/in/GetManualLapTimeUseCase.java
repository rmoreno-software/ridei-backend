package com.ridei.sessions.application.port.in;

import com.ridei.sessions.domain.model.ManualLapTime;
import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.RiderId;

public interface GetManualLapTimeUseCase {
    ManualLapTime get(RiderId riderId, ManualLapTimeId id);
}
