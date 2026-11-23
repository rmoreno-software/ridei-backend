package com.ridei.sessions.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.sessions.application.port.in.ImportManualLapTimesUseCase;
import com.ridei.sessions.application.port.in.RecordManualLapTimeUseCase;
import com.ridei.sessions.domain.exception.LayoutNotFoundException;
import com.ridei.sessions.domain.exception.ManualLapTimeIdConflictException;
import com.ridei.sessions.domain.exception.MotorbikeNotFoundException;
import com.ridei.sessions.domain.model.CatalogVenue;
import com.ridei.sessions.domain.model.LayoutRef;
import com.ridei.sessions.domain.model.ManualLapTime;
import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;
import com.ridei.sessions.domain.model.VenueId;
import com.ridei.sessions.domain.port.out.GaragePort;
import com.ridei.sessions.domain.port.out.ManualLapTimeRepositoryPort;
import com.ridei.sessions.domain.port.out.VenueCatalogPort;

/**
 * Apunta tiempos manuales, de uno en uno (22B) o importados de golpe (22C). Igual que al subir una
 * sesión, el trazado debe existir en el catálogo y la moto estar en el garaje del piloto (puede ser
 * una que ya no tiene); se congela su nombre. Idempotente por id.
 */
public class RecordManualLapTimesService implements RecordManualLapTimeUseCase, ImportManualLapTimesUseCase {

    public static final int MAX_BATCH = 100;

    private final ManualLapTimeRepositoryPort repository;
    private final VenueCatalogPort catalog;
    private final GaragePort garage;
    private final Clock clock;

    public RecordManualLapTimesService(ManualLapTimeRepositoryPort repository, VenueCatalogPort catalog, GaragePort garage, Clock clock) {
        this.repository = repository;
        this.catalog = catalog;
        this.garage = garage;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RecordManualLapTimeResult record(RecordManualLapTimeCommand command) {
        return record(command, new Lookups());
    }

    @Override
    @Transactional
    public List<RecordManualLapTimeResult> recordAll(List<RecordManualLapTimeCommand> commands) {
        if (commands == null || commands.isEmpty()) throw new IllegalArgumentException("At least one lap time is required");
        if (commands.size() > MAX_BATCH) throw new IllegalArgumentException("At most " + MAX_BATCH + " lap times at once");
        Set<ManualLapTimeId> ids = new HashSet<>();
        for (RecordManualLapTimeCommand command : commands) {
            if (!ids.add(command.id())) throw new IllegalArgumentException("Repeated lap time id " + command.id().value());
        }
        Lookups lookups = new Lookups();
        List<RecordManualLapTimeResult> results = new ArrayList<>();
        for (RecordManualLapTimeCommand command : commands) results.add(record(command, lookups));
        return results;
    }

    private RecordManualLapTimeResult record(RecordManualLapTimeCommand command, Lookups lookups) {
        Optional<ManualLapTime> existing = repository.findById(command.id());
        if (existing.isPresent()) {
            if (!existing.get().isOwnedBy(command.riderId())) throw new ManualLapTimeIdConflictException();
            return new RecordManualLapTimeResult(existing.get(), false);
        }
        MotorbikeSnapshot motorbike = lookups.motorbike(command.motorbikeId());
        LayoutRef layout = lookups.venue(command.venueId()).layout(command.layoutId()).orElseThrow(LayoutNotFoundException::new);
        ManualLapTime time = ManualLapTime.record(
            command.id(), command.riderId(), layout, motorbike, command.entry(), LocalDate.now(clock), clock.instant());
        repository.save(time);
        return new RecordManualLapTimeResult(time, true);
    }

    /** En una importación casi todos los tiempos comparten moto y muchos circuito: se consulta una vez. */
    private final class Lookups {
        private final Map<VenueId, CatalogVenue> venues = new HashMap<>();
        private final Map<MotorbikeId, MotorbikeSnapshot> motorbikes = new HashMap<>();

        CatalogVenue venue(VenueId id) {
            CatalogVenue venue = venues.get(id);
            if (venue == null) {
                venue = catalog.findVenue(id).orElseThrow(LayoutNotFoundException::new);
                venues.put(id, venue);
            }
            return venue;
        }

        MotorbikeSnapshot motorbike(MotorbikeId id) {
            MotorbikeSnapshot motorbike = motorbikes.get(id);
            if (motorbike == null) {
                motorbike = garage.findOwnMotorbike(id).orElseThrow(MotorbikeNotFoundException::new);
                motorbikes.put(id, motorbike);
            }
            return motorbike;
        }
    }
}
