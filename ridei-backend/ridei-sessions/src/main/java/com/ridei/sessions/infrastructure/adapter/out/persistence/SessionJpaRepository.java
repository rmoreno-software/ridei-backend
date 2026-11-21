package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SessionJpaRepository extends JpaRepository<SessionJpaEntity, UUID> {

    List<SessionJpaEntity> findByRiderIdOrderByStartedAtDesc(UUID riderId);
}
