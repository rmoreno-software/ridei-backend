package com.ridei.landing.infrastructure.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistJpaRepository extends JpaRepository<WaitlistEntryJpaEntity, UUID> {
    boolean existsByEmail(String email);
}
