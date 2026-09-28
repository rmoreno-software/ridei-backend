package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BrandJpaRepository extends JpaRepository<BrandJpaEntity, UUID> {
    @Query(value = """
        SELECT b.id AS id, b.name AS name, COUNT(m.id) AS motorbike_count
        FROM brands b
        LEFT JOIN motorbikes m ON m.brand_id = b.id
        GROUP BY b.id, b.name
        ORDER BY b.name
            """, nativeQuery = true) 
    List<BrandUsageProjection> findAllWithMotorbikeCount();
}
