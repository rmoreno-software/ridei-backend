package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.ridei.sessions.domain.model.LapTimePrecision;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.LayoutRef;
import com.ridei.sessions.domain.model.ManualLapEntry;
import com.ridei.sessions.domain.model.ManualLapTime;
import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.TimeSource;
import com.ridei.sessions.domain.model.TrackCondition;
import com.ridei.sessions.domain.model.VenueId;
import com.ridei.sessions.domain.port.out.ManualLapTimeRepositoryPort;

@Component
class ManualLapTimePersistenceAdapter implements ManualLapTimeRepositoryPort {

    private final ManualLapTimeJpaRepository repository;

    ManualLapTimePersistenceAdapter(ManualLapTimeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(ManualLapTime time) {
        repository.save(toEntity(time));
    }

    @Override
    public Optional<ManualLapTime> findById(ManualLapTimeId id) {
        return repository.findById(id.value()).map(ManualLapTimePersistenceAdapter::toDomain);
    }

    @Override
    public List<ManualLapTime> findByRider(RiderId riderId) {
        return repository.findByRiderIdOrderByLapDateDescCreatedAtDesc(riderId.value()).stream()
            .map(ManualLapTimePersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public void delete(ManualLapTimeId id) {
        repository.deleteById(id.value());
    }

    private static ManualLapTimeJpaEntity toEntity(ManualLapTime t) {
        ManualLapTimeJpaEntity e = new ManualLapTimeJpaEntity();
        ManualLapEntry entry = t.entry();
        e.id = t.id().value();
        e.riderId = t.riderId().value();
        e.venueId = t.layout().venueId().value();
        e.venueName = t.layout().venueName();
        e.trackName = t.layout().trackName();
        e.layoutId = t.layout().layoutId().value();
        e.layoutName = t.layout().layoutName();
        e.motorbikeId = t.motorbike().motorbikeId().value();
        e.motorbikeName = t.motorbike().displayName();
        e.motorbikeCategory = t.motorbike().category();
        e.motorbikeCategoryGroup = t.motorbike().categoryGroup();
        e.lapDate = entry.date();
        e.bestLapMs = entry.bestLapMillis();
        e.lapPrecision = entry.precision().name();
        e.timeSource = entry.source().name();
        e.lapCount = entry.lapCount();
        e.trackCondition = entry.trackCondition() != null ? entry.trackCondition().name() : null;
        e.tyres = entry.tyres();
        e.otherLapsMs = entry.otherLapsMillis().toArray(Long[]::new);
        e.notes = entry.notes();
        e.createdAt = t.createdAt();
        return e;
    }

    private static ManualLapTime toDomain(ManualLapTimeJpaEntity e) {
        LayoutRef layout = new LayoutRef(VenueId.of(e.venueId), e.venueName, e.trackName, LayoutId.of(e.layoutId), e.layoutName);
        MotorbikeSnapshot motorbike = new MotorbikeSnapshot(
            MotorbikeId.of(e.motorbikeId), e.motorbikeName, e.motorbikeCategory, e.motorbikeCategoryGroup);
        ManualLapEntry entry = new ManualLapEntry(
            e.lapDate, e.bestLapMs, LapTimePrecision.valueOf(e.lapPrecision), TimeSource.valueOf(e.timeSource), e.lapCount,
            e.trackCondition != null ? TrackCondition.valueOf(e.trackCondition) : null, e.tyres,
            e.otherLapsMs != null ? Arrays.asList(e.otherLapsMs) : List.of(), e.notes);
        return ManualLapTime.reconstitute(ManualLapTimeId.of(e.id), RiderId.of(e.riderId), layout, motorbike, entry, e.createdAt);
    }
}
