package com.ridei.garage.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ridei.garage.domain.exception.MotorbikeNotFoundException;
import com.ridei.garage.domain.model.Category;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.MotorbikeSetup;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.SetupComponent;
import com.ridei.garage.domain.model.TyreSpec;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikeSetupRepositoryPort;

class RegisterSetupChangeServiceTest {

    private static final OwnerId OWNER = new OwnerId(UUID.randomUUID());
    private static final LocalDate MOUNTED_ON = LocalDate.of(2026, 10, 3);

    private final MotorbikeRepositoryPort motorbikeRepository = mock(MotorbikeRepositoryPort.class);
    private final MotorbikeSetupRepositoryPort setupRepository = mock(MotorbikeSetupRepositoryPort.class);
    private final RegisterSetupChangeService service =
        new RegisterSetupChangeService(motorbikeRepository, setupRepository);

    private Motorbike motorbike;

    @BeforeEach
    void setUp() {
        motorbike = Motorbike.register(OWNER, null, "Yamaha", "YZF-R6", Category.SPEED, 2018, 599, null,
            null, null, false);
        when(motorbikeRepository.findById(motorbike.getId())).thenReturn(Optional.of(motorbike));
        when(setupRepository.findByMotorbikeId(motorbike.getId()))
            .thenReturn(MotorbikeSetup.reconstitute(motorbike.getId(), List.of()));
    }

    @Test
    void changingBothTyresMountsEachOneOnTheSameDateAndSaves() {
        TyreSpec front = new TyreSpec("Michelin Power Cup 2", "120/70 ZR17");
        TyreSpec rear = new TyreSpec("Michelin Power Cup 2", "180/55 ZR17");

        MotorbikeSetup setup = service.register(new RegisterSetupChangeCommand(
            OWNER, motorbike.getId(), MOUNTED_ON,
            Map.of(SetupComponent.FRONT_TYRE, front, SetupComponent.REAR_TYRE, rear)
        ));

        assertThat(setup.current(SetupComponent.FRONT_TYRE)).hasValueSatisfying(e -> {
            assertThat(e.getSpec()).isEqualTo(front);
            assertThat(e.getMountedOn()).isEqualTo(MOUNTED_ON);
        });
        assertThat(setup.current(SetupComponent.REAR_TYRE)).hasValueSatisfying(e ->
            assertThat(e.getSpec()).isEqualTo(rear));
        assertThat(setup.current(SetupComponent.FINAL_DRIVE)).isEmpty();
        verify(setupRepository).save(setup);
    }

    @Test
    void motorbikeOfAnotherOwnerIsReportedAsNotFound() {
        OwnerId stranger = new OwnerId(UUID.randomUUID());

        assertThatThrownBy(() -> service.register(new RegisterSetupChangeCommand(
            stranger, motorbike.getId(), MOUNTED_ON,
            Map.of(SetupComponent.REAR_TYRE, new TyreSpec("Michelin Power Cup 2", null))
        ))).isInstanceOf(MotorbikeNotFoundException.class);

        verify(setupRepository, never()).save(any());
    }

    @Test
    void commandWithoutChangesIsRejected() {
        MotorbikeId motorbikeId = motorbike.getId();

        assertThatThrownBy(() -> new RegisterSetupChangeCommand(OWNER, motorbikeId, MOUNTED_ON, Map.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
