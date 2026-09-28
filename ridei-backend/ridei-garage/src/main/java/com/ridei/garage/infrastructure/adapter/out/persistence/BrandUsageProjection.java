package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.util.UUID;

public interface BrandUsageProjection {
    UUID getId();
    String getName();
    long getMotorbikeCount();
}
