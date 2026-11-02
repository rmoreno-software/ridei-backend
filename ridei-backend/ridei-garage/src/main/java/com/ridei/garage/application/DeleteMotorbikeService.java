package com.ridei.garage.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.application.port.in.DeleteMotorbikeUseCase;
import com.ridei.garage.domain.event.MotorbikeDeletedEvent;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.port.out.EventPublisherPort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikeSetupRepositoryPort;

/**
 * Elimina una moto con su setup y sus fotos.
 *
 * <p>Todo lo que vive en la base de datos (setup, filas de fotos, moto) se borra en esta
 * transacción. Las fotos del storage no son transaccionales: se borran al recibir
 * {@link MotorbikeDeletedEvent}, que la infraestructura entrega solo tras el commit. Así un
 * fallo del storage nunca deja una moto viva con fotos rotas; como mucho deja objetos huérfanos.
 */
public class DeleteMotorbikeService implements DeleteMotorbikeUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;
    private final MotorbikeSetupRepositoryPort setupRepository;
    private final EventPublisherPort eventPublisher;

    public DeleteMotorbikeService(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikeSetupRepositoryPort setupRepository,
        EventPublisherPort eventPublisher
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.setupRepository = setupRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void delete(DeleteMotorbikeCommand command) {
        Motorbike motorbike = OwnedMotorbikes.require(
            motorbikeRepository,
            command.ownerId(),
            command.motorbikeId()
        );

        // El setup es otro agregado que referencia a la moto: se borra antes que ella
        setupRepository.deleteByMotorbikeId(motorbike.getId());
        motorbikeRepository.delete(motorbike);

        eventPublisher.publish(MotorbikeDeletedEvent.of(motorbike.getOwnerId(), motorbike.getId()));
    }
}
