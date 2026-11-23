package com.ridei.sessions.application;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.sessions.application.port.in.DeleteManualLapTimeUseCase;
import com.ridei.sessions.application.port.in.GetManualLapTimeUseCase;
import com.ridei.sessions.application.port.in.ListManualLapTimesUseCase;
import com.ridei.sessions.domain.exception.ManualLapTimeNotFoundException;
import com.ridei.sessions.domain.model.ManualLapTime;
import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.port.out.ManualLapTimeRepositoryPort;

/** Consultar y borrar tiempos manuales propios. Anti-IDOR: los de otro piloto responden 404. */
public class ManualLapTimesService implements ListManualLapTimesUseCase, GetManualLapTimeUseCase, DeleteManualLapTimeUseCase {

    private final ManualLapTimeRepositoryPort repository;

    public ManualLapTimesService(ManualLapTimeRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManualLapTime> list(ListManualLapTimesQuery query) {
        return repository.findByRider(query.riderId()).stream()
            .filter(t -> query.layoutId() == null || t.layout().layoutId().equals(query.layoutId()))
            .filter(t -> query.motorbikeId() == null || t.motorbike().motorbikeId().equals(query.motorbikeId()))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ManualLapTime get(RiderId riderId, ManualLapTimeId id) {
        return require(riderId, id);
    }

    @Override
    @Transactional
    public void delete(RiderId riderId, ManualLapTimeId id) {
        require(riderId, id);
        repository.delete(id);
    }

    private ManualLapTime require(RiderId riderId, ManualLapTimeId id) {
        return repository.findById(id).filter(t -> t.isOwnedBy(riderId)).orElseThrow(ManualLapTimeNotFoundException::new);
    }
}
