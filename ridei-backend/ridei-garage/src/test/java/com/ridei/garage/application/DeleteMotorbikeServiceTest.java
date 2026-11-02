package com.ridei.garage.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.ridei.garage.domain.event.MotorbikeDeletedEvent;
import com.ridei.garage.domain.exception.MotorbikeNotFoundException;
import com.ridei.garage.domain.model.Category;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.port.out.EventPublisherPort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikeSetupRepositoryPort;

class DeleteMotorbikeServiceTest {

    private static final OwnerId OWNER = new OwnerId(UUID.randomUUID());

    private final MotorbikeRepositoryPort motorbikeRepository = mock(MotorbikeRepositoryPort.class);
    private final MotorbikeSetupRepositoryPort setupRepository = mock(MotorbikeSetupRepositoryPort.class);
    private final EventPublisherPort eventPublisher = mock(EventPublisherPort.class);
    private final DeleteMotorbikeService service =
        new DeleteMotorbikeService(motorbikeRepository, setupRepository, eventPublisher);

    private Motorbike motorbike;

    @BeforeEach
    void setUp() {
        motorbike = Motorbike.register(OWNER, null, "Yamaha", "YZF-R6", Category.SPEED, 2018, 599, null,
            null, null, true);
        motorbike.addPhoto("https://cdn.example/garage/photo-1.jpg", null);
        when(motorbikeRepository.findById(motorbike.getId())).thenReturn(Optional.of(motorbike));
    }

    @Test
    void deletesSetupBeforeTheMotorbike() {
        service.delete(new DeleteMotorbikeCommand(OWNER, motorbike.getId()));

        InOrder order = inOrder(setupRepository, motorbikeRepository);
        order.verify(setupRepository).deleteByMotorbikeId(motorbike.getId());
        order.verify(motorbikeRepository).delete(motorbike);
    }

    @Test
    void publishesDeletionEventSoPhotosArePurgedFromStorage() {
        service.delete(new DeleteMotorbikeCommand(OWNER, motorbike.getId()));

        ArgumentCaptor<MotorbikeDeletedEvent> event = ArgumentCaptor.forClass(MotorbikeDeletedEvent.class);
        verify(eventPublisher).publish(event.capture());
        assertThat(event.getValue().ownerId()).isEqualTo(OWNER);
        assertThat(event.getValue().motorbikeId()).isEqualTo(motorbike.getId());
        assertThat(event.getValue().occurredAt()).isNotNull();
    }

    @Test
    void motorbikeOfAnotherOwnerIsNotFoundAndNothingIsDeleted() {
        OwnerId stranger = new OwnerId(UUID.randomUUID());

        assertThatThrownBy(() -> service.delete(new DeleteMotorbikeCommand(stranger, motorbike.getId())))
            .isInstanceOf(MotorbikeNotFoundException.class);

        verifyNoInteractions(setupRepository, eventPublisher);
        verify(motorbikeRepository, never()).delete(any());
    }

    @Test
    void unknownMotorbikeIsNotFoundAndNothingIsDeleted() {
        MotorbikeId unknown = MotorbikeId.newId();
        when(motorbikeRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(new DeleteMotorbikeCommand(OWNER, unknown)))
            .isInstanceOf(MotorbikeNotFoundException.class);

        verifyNoInteractions(setupRepository, eventPublisher);
    }
}
