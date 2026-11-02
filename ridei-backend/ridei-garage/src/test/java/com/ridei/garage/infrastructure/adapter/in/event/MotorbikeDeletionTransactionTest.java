package com.ridei.garage.infrastructure.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.ridei.garage.application.DeleteMotorbikeCommand;
import com.ridei.garage.application.DeleteMotorbikeService;
import com.ridei.garage.application.port.in.DeleteMotorbikeUseCase;
import com.ridei.garage.application.port.in.PurgeMotorbikePhotosUseCase;
import com.ridei.garage.domain.event.MotorbikeDeletedEvent;
import com.ridei.garage.domain.model.Category;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.port.out.EventPublisherPort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikeSetupRepositoryPort;
import com.ridei.garage.infrastructure.adapter.out.event.SpringEventPublisherAdapter;

/**
 * Comprueba el cableado real (proxy {@code @Transactional} + {@code @TransactionalEventListener})
 * con un contexto Spring mínimo y un gestor de transacciones en memoria: sin base de datos.
 */
class MotorbikeDeletionTransactionTest {

    private static final OwnerId OWNER = new OwnerId(UUID.randomUUID());

    private static AnnotationConfigApplicationContext context;
    private static DeleteMotorbikeUseCase deleteMotorbike;
    private static MotorbikeRepositoryPort motorbikeRepository;
    private static PurgeMotorbikePhotosUseCase purgePhotos;

    private Motorbike motorbike;

    @BeforeAll
    static void startContext() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        deleteMotorbike = context.getBean(DeleteMotorbikeUseCase.class);
        motorbikeRepository = context.getBean(MotorbikeRepositoryPort.class);
        purgePhotos = context.getBean(PurgeMotorbikePhotosUseCase.class);
    }

    @AfterAll
    static void closeContext() {
        context.close();
    }

    @BeforeEach
    void setUp() {
        reset(motorbikeRepository, purgePhotos);
        motorbike = Motorbike.register(OWNER, null, "KTM", "Duke 390", Category.SUPERMOTARD, 2021, 373, null,
            null, null, false);
        when(motorbikeRepository.findById(motorbike.getId())).thenReturn(Optional.of(motorbike));
    }

    @Test
    void photosArePurgedOnceTheDeletionCommits() {
        deleteMotorbike.delete(new DeleteMotorbikeCommand(OWNER, motorbike.getId()));

        verify(purgePhotos).purge(any(MotorbikeDeletedEvent.class));
    }

    @Test
    void photosAreKeptWhenTheDeletionRollsBack() {
        doThrow(new IllegalStateException("database down")).when(motorbikeRepository).delete(motorbike);

        assertThatThrownBy(() -> deleteMotorbike.delete(new DeleteMotorbikeCommand(OWNER, motorbike.getId())))
            .isInstanceOf(IllegalStateException.class);

        verify(purgePhotos, never()).purge(any());
    }

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {

        @Bean
        PlatformTransactionManager transactionManager() {
            return new InMemoryTransactionManager();
        }

        @Bean
        MotorbikeRepositoryPort motorbikeRepository() {
            return mock(MotorbikeRepositoryPort.class);
        }

        @Bean
        MotorbikeSetupRepositoryPort setupRepository() {
            return mock(MotorbikeSetupRepositoryPort.class);
        }

        @Bean
        PurgeMotorbikePhotosUseCase purgeMotorbikePhotosUseCase() {
            return mock(PurgeMotorbikePhotosUseCase.class);
        }

        @Bean
        EventPublisherPort eventPublisher(ApplicationEventPublisher publisher) {
            return new SpringEventPublisherAdapter(publisher);
        }

        @Bean
        DeleteMotorbikeUseCase deleteMotorbikeUseCase(
            MotorbikeRepositoryPort motorbikeRepository,
            MotorbikeSetupRepositoryPort setupRepository,
            EventPublisherPort eventPublisher
        ) {
            return new DeleteMotorbikeService(motorbikeRepository, setupRepository, eventPublisher);
        }

        @Bean
        MotorbikeDeletedListener motorbikeDeletedListener(PurgeMotorbikePhotosUseCase purge) {
            return new MotorbikeDeletedListener(purge);
        }
    }

    /** Transacciones sin recurso real: basta para que Spring gestione la sincronización. */
    static class InMemoryTransactionManager extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
        }
    }
}
