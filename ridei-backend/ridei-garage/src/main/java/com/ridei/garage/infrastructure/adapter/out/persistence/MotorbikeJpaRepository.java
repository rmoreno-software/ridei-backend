package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MotorbikeJpaRepository extends JpaRepository<MotorbikeJpaEntity, UUID> {
    List<MotorbikeJpaEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}
