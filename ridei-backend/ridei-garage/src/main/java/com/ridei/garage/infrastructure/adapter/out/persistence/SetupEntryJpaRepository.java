package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SetupEntryJpaRepository extends JpaRepository<SetupEntryJpaEntity, UUID> {

    List<SetupEntryJpaEntity> findAllByMotorbikeId(UUID motorbikeId);

    @Query("select e.id from SetupEntryJpaEntity e where e.motorbikeId = :motorbikeId")
    List<UUID> findIdsByMotorbikeId(@Param("motorbikeId") UUID motorbikeId);

    @Modifying
    @Query("delete from SetupEntryJpaEntity e where e.motorbikeId = :motorbikeId")
    int deleteAllByMotorbikeId(@Param("motorbikeId") UUID motorbikeId);
}
