package com.ridei.garage.application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ridei.garage.domain.event.MotorbikeDeletedEvent;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.port.out.MotorbikePhotoStoragePort;

class PurgeMotorbikePhotosServiceTest {

    private final MotorbikePhotoStoragePort storage = mock(MotorbikePhotoStoragePort.class);
    private final PurgeMotorbikePhotosService service = new PurgeMotorbikePhotosService(storage);

    @Test
    void deletesEveryStoredObjectOfTheMotorbike() {
        OwnerId owner = new OwnerId(UUID.randomUUID());
        MotorbikeId motorbikeId = MotorbikeId.newId();

        service.purge(MotorbikeDeletedEvent.of(owner, motorbikeId));

        verify(storage).deleteAllOf(owner, motorbikeId);
        verifyNoMoreInteractions(storage);
    }
}
