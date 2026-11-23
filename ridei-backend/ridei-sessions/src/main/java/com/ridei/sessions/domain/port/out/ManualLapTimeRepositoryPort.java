package com.ridei.sessions.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.ridei.sessions.domain.model.ManualLapTime;
import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.RiderId;

public interface ManualLapTimeRepositoryPort {

    void save(ManualLapTime time);

    Optional<ManualLapTime> findById(ManualLapTimeId id);

    /** Todos los tiempos manuales del piloto, del más reciente al más antiguo. */
    List<ManualLapTime> findByRider(RiderId riderId);

    void delete(ManualLapTimeId id);
}
