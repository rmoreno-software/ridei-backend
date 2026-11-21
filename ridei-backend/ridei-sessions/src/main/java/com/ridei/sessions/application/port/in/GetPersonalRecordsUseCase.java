package com.ridei.sessions.application.port.in;

import java.util.List;

import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.PersonalRecord;
import com.ridei.sessions.domain.model.RiderId;

public interface GetPersonalRecordsUseCase {
    /** Filtros opcionales ({@code null} = todos). */
    List<PersonalRecord> records(RiderId riderId, LayoutId layoutId, MotorbikeId motorbikeId);
}
