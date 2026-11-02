package com.ridei.garage.domain.port.out;

import com.ridei.garage.domain.model.ImageContentType;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.PresignedUpload;

public interface MotorbikePhotoStoragePort {
    PresignedUpload createUploadUrl(
        OwnerId ownerId,
        MotorbikeId motorbikeId,
        ImageContentType contentType   
    );
    boolean belongsToMotorbike(
        OwnerId ownerId,
        MotorbikeId motorbikeId,
        String publicUrl
    );
    long getContentLength(String publicUrl);
    void delete(String publicUrl);
    /**
     * Borra todos los objetos guardados para la moto, también subidas presignadas que nunca
     * se confirmaron. Idempotente: no falla si no queda nada.
     */
    void deleteAllOf(OwnerId ownerId, MotorbikeId motorbikeId);
}
