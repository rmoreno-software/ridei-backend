package com.ridei.garage.domain.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

class MotorbikeDeletedEventTest {

    @Test
    void requiresOwnerMotorbikeAndTimestamp() {
        OwnerId owner = new OwnerId(UUID.randomUUID());
        MotorbikeId motorbikeId = MotorbikeId.newId();

        assertThatThrownBy(() -> new MotorbikeDeletedEvent(null, motorbikeId, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MotorbikeDeletedEvent(owner, null, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MotorbikeDeletedEvent(owner, motorbikeId, null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
