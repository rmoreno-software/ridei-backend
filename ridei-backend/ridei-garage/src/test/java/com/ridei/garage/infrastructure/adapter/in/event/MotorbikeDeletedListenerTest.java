package com.ridei.garage.infrastructure.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ridei.garage.application.port.in.PurgeMotorbikePhotosUseCase;
import com.ridei.garage.domain.event.MotorbikeDeletedEvent;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

class MotorbikeDeletedListenerTest {

    private final PurgeMotorbikePhotosUseCase purge = mock(PurgeMotorbikePhotosUseCase.class);
    private final MotorbikeDeletedListener listener = new MotorbikeDeletedListener(purge);
    private final MotorbikeDeletedEvent event =
        MotorbikeDeletedEvent.of(new OwnerId(UUID.randomUUID()), MotorbikeId.newId());

    @Test
    void purgesPhotosOfTheDeletedMotorbike() {
        listener.on(event);

        verify(purge).purge(event);
    }

    @Test
    void storageFailureIsLoggedAndNotPropagated() {
        doThrow(new RuntimeException("R2 unavailable")).when(purge).purge(event);

        assertThatCode(() -> listener.on(event)).doesNotThrowAnyException();
    }

    /** Si se ejecutara antes del commit, un rollback dejaría la moto viva y sin fotos. */
    @Test
    void runsOnlyAfterTheDeletionIsCommitted() throws NoSuchMethodException {
        Method on = MotorbikeDeletedListener.class.getMethod("on", MotorbikeDeletedEvent.class);

        TransactionalEventListener annotation = on.getAnnotation(TransactionalEventListener.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
        assertThat(annotation.fallbackExecution()).isFalse();
    }
}
