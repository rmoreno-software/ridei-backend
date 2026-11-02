package com.ridei.garage.application.port.in;

import com.ridei.garage.domain.event.MotorbikeDeletedEvent;

/** Limpia del storage las fotos de una moto ya eliminada. */
public interface PurgeMotorbikePhotosUseCase {
    void purge(MotorbikeDeletedEvent event);
}
