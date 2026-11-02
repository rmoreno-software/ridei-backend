package com.ridei.garage.infrastructure.adapter.in.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ridei.garage.application.port.in.PurgeMotorbikePhotosUseCase;
import com.ridei.garage.domain.event.MotorbikeDeletedEvent;

import lombok.AllArgsConstructor;

/**
 * Adaptador de entrada: al confirmarse el borrado de una moto, limpia sus fotos del storage.
 *
 * <p>{@code AFTER_COMMIT}: si la transacción se deshace, las fotos siguen en su sitio. Un fallo
 * aquí ya no puede afectar a la respuesta del DELETE (la moto ya no existe), así que se registra
 * y se sigue: el coste es dejar objetos huérfanos en el bucket, localizables por el log.
 */
@Component
@AllArgsConstructor
public class MotorbikeDeletedListener {

    private static final Logger log = LoggerFactory.getLogger(MotorbikeDeletedListener.class);

    private final PurgeMotorbikePhotosUseCase purgeMotorbikePhotosUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MotorbikeDeletedEvent event) {
        try {
            purgeMotorbikePhotosUseCase.purge(event);
        } catch (RuntimeException e) {
            log.error("Could not purge stored photos of deleted motorbike {} (owner {}); objects left orphaned",
                event.motorbikeId().value(), event.ownerId().value(), e);
        }
    }
}
