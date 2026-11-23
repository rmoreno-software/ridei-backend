package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ManualLapTimeJpaRepository extends JpaRepository<ManualLapTimeJpaEntity, UUID> {

    List<ManualLapTimeJpaEntity> findByRiderIdOrderByLapDateDescCreatedAtDesc(UUID riderId);
}
