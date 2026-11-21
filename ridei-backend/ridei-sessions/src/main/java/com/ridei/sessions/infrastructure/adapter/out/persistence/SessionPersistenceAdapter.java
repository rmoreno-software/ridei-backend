package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridei.sessions.domain.model.Conditions;
import com.ridei.sessions.domain.model.Gate;
import com.ridei.sessions.domain.model.GeoPoint;
import com.ridei.sessions.domain.model.IdealLap;
import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.LapTiming;
import com.ridei.sessions.domain.model.LapValidity;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.LayoutSnapshot;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.model.TrackCondition;
import com.ridei.sessions.domain.model.TrackGeometry;
import com.ridei.sessions.domain.model.VenueId;
import com.ridei.sessions.domain.port.out.SessionRepositoryPort;

@Component
class SessionPersistenceAdapter implements SessionRepositoryPort {

    private static final TypeReference<List<double[]>> POINTS = new TypeReference<>() {};

    private final SessionJpaRepository repository;
    private final ObjectMapper objectMapper;

    SessionPersistenceAdapter(SessionJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(Session session) {
        SessionJpaEntity entity = repository.findById(session.id().value()).orElseGet(SessionJpaEntity::new);
        copy(session, entity);
        repository.save(entity);
    }

    @Override
    public Optional<Session> findById(SessionId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Session> findByRider(RiderId riderId) {
        return repository.findByRiderIdOrderByStartedAtDesc(riderId.value()).stream().map(this::toDomain).toList();
    }

    @Override
    public void delete(SessionId id) {
        repository.deleteById(id.value());
    }

    // ── Dominio → JPA ───────────────────────────────────────────────────────────────────

    private void copy(Session s, SessionJpaEntity e) {
        LayoutSnapshot layout = s.layout();
        MotorbikeSnapshot motorbike = s.motorbike();
        e.id = s.id().value();
        e.riderId = s.riderId().value();
        e.name = s.name();
        e.venueId = layout.venueId().value();
        e.venueName = layout.venueName();
        e.trackName = layout.trackName();
        e.layoutId = layout.layoutId().value();
        e.layoutName = layout.layoutName();
        e.layoutPath = writePoints(layout.geometry().path());
        e.finishLine = writePoints(List.of(layout.geometry().finishLine().left(), layout.geometry().finishLine().right()));
        e.sectorCount = layout.geometry().sectorCount();
        e.motorbikeId = motorbike.motorbikeId().value();
        e.motorbikeName = motorbike.displayName();
        e.motorbikeCategory = motorbike.category();
        e.motorbikeCategoryGroup = motorbike.categoryGroup();
        e.startedAt = s.startedAt();
        e.endedAt = s.endedAt();
        e.temperatureCelsius = s.conditions().temperatureCelsius();
        e.trackCondition = s.conditions().trackCondition() != null ? s.conditions().trackCondition().name() : null;
        e.notes = s.notes();
        e.reversedDirection = s.timing().reversedDirection();
        e.bestLapMs = s.bestLap().map(Lap::durationMillis).orElse(null);
        e.idealLapMs = s.idealLap().map(IdealLap::totalMillis).orElse(null);
        e.validLapCount = s.timing().validLaps().size();
        e.laps.clear();
        s.timing().laps().forEach(lap -> e.laps.add(toEmbeddable(lap)));
        e.createdAt = s.createdAt();
        e.updatedAt = s.updatedAt();
    }

    private static LapEmbeddable toEmbeddable(Lap lap) {
        LapEmbeddable e = new LapEmbeddable();
        e.lapNumber = lap.number();
        e.startedAt = lap.startedAt();
        e.durationMs = lap.durationMillis();
        e.sectorMs = lap.sectorMillis().toArray(Long[]::new);
        e.distanceM = Math.round(lap.distanceMeters() * 10) / 10.0;
        e.validity = lap.validity().name();
        return e;
    }

    // ── JPA → dominio ───────────────────────────────────────────────────────────────────

    private Session toDomain(SessionJpaEntity e) {
        List<GeoPoint> finish = readPoints(e.finishLine);
        TrackGeometry geometry = TrackGeometry.of(Gate.of(finish.get(0), finish.get(1)), readPoints(e.layoutPath), e.sectorCount);
        LayoutSnapshot layout = new LayoutSnapshot(
            VenueId.of(e.venueId), e.venueName, e.trackName, LayoutId.of(e.layoutId), e.layoutName, geometry);
        MotorbikeSnapshot motorbike = new MotorbikeSnapshot(
            MotorbikeId.of(e.motorbikeId), e.motorbikeName, e.motorbikeCategory, e.motorbikeCategoryGroup);
        Conditions conditions = new Conditions(
            e.temperatureCelsius, e.trackCondition != null ? TrackCondition.valueOf(e.trackCondition) : null);
        List<Lap> laps = e.laps.stream()
            .map(l -> new Lap(l.lapNumber, l.startedAt, l.durationMs, Arrays.asList(l.sectorMs), l.distanceM, LapValidity.valueOf(l.validity)))
            .toList();
        return Session.reconstitute(
            SessionId.of(e.id), RiderId.of(e.riderId), layout, motorbike, e.name, conditions, e.notes,
            e.startedAt, e.endedAt, new LapTiming(laps, e.sectorCount, e.reversedDirection), e.createdAt, e.updatedAt);
    }

    private String writePoints(List<GeoPoint> points) {
        try {
            return objectMapper.writeValueAsString(points.stream().map(p -> new double[] {p.latitude(), p.longitude()}).toList());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize layout geometry", e);
        }
    }

    private List<GeoPoint> readPoints(String json) {
        try {
            return objectMapper.readValue(json, POINTS).stream().map(p -> GeoPoint.of(p[0], p[1])).toList();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Corrupt layout geometry", e);
        }
    }
}
