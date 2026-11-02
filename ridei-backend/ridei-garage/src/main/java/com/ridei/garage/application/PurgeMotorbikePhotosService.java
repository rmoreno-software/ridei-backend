package com.ridei.garage.application;

import com.ridei.garage.application.port.in.PurgeMotorbikePhotosUseCase;
import com.ridei.garage.domain.event.MotorbikeDeletedEvent;
import com.ridei.garage.domain.port.out.MotorbikePhotoStoragePort;

/**
 * Borra del storage todos los objetos de una moto eliminada. Se borra por prefijo de la moto,
 * no por la lista de fotos: así caen también las subidas presignadas que nunca se confirmaron.
 */
public class PurgeMotorbikePhotosService implements PurgeMotorbikePhotosUseCase {

    private final MotorbikePhotoStoragePort photoStorage;

    public PurgeMotorbikePhotosService(MotorbikePhotoStoragePort photoStorage) {
        this.photoStorage = photoStorage;
    }

    @Override
    public void purge(MotorbikeDeletedEvent event) {
        photoStorage.deleteAllOf(event.ownerId(), event.motorbikeId());
    }
}
