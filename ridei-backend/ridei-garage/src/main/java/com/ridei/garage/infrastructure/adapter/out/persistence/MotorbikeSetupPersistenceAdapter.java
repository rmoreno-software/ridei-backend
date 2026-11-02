package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.MotorbikeSetup;
import com.ridei.garage.domain.port.out.MotorbikeSetupRepositoryPort;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MotorbikeSetupPersistenceAdapter implements MotorbikeSetupRepositoryPort {

    private final SetupEntryJpaRepository jpaRepository;

    @Override
    public MotorbikeSetup findByMotorbikeId(MotorbikeId motorbikeId) {
        return MotorbikeSetup.reconstitute(
            motorbikeId,
            jpaRepository.findAllByMotorbikeId(motorbikeId.value()).stream()
                .map(SetupEntryJpaEntity::toDomain)
                .toList()
        );
    }

    /**
     * Las entradas existentes se guardan y se vuelcan a la BD antes de insertar las nuevas.
     * Hibernate ejecuta los INSERT antes que los UPDATE en un mismo flush; sin este orden, la
     * pieza nueva chocaría con el índice único parcial {@code uq_setup_entries_current} mientras
     * la anterior todavía no tiene {@code removed_on}.
     */
    @Override
    public void save(MotorbikeSetup setup) {
        Set<UUID> persistedIds = new HashSet<>(jpaRepository.findIdsByMotorbikeId(setup.getMotorbikeId().value()));

        Map<Boolean, List<SetupEntryJpaEntity>> byPersisted = setup.getEntries().stream()
            .map(entry -> SetupEntryJpaEntity.fromDomain(setup.getMotorbikeId(), entry))
            .collect(Collectors.partitioningBy(entity -> persistedIds.contains(entity.getId())));

        jpaRepository.saveAllAndFlush(byPersisted.get(true));
        jpaRepository.saveAll(byPersisted.get(false));
    }

    @Override
    public void deleteByMotorbikeId(MotorbikeId motorbikeId) {
        jpaRepository.deleteAllByMotorbikeId(motorbikeId.value());
    }
}
