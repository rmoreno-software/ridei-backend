package com.ridei.garage.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ridei.garage.domain.exception.SetupChangeDateConflictException;
import com.ridei.garage.domain.exception.SetupEntryNotFoundException;

class MotorbikeSetupTest {

    private static final LocalDate SEP_02 = LocalDate.of(2026, 9, 2);
    private static final LocalDate SEP_19 = LocalDate.of(2026, 9, 19);
    private static final LocalDate OCT_03 = LocalDate.of(2026, 10, 3);

    private static final TyreSpec POWER_CUP = new TyreSpec("Michelin Power Cup 2", "180/55 ZR17");
    private static final TyreSpec SUPERCORSA = new TyreSpec("Pirelli Diablo Supercorsa SP", "180/55 ZR17");

    private static MotorbikeSetup emptySetup() {
        return MotorbikeSetup.reconstitute(new MotorbikeId(UUID.randomUUID()), List.of());
    }

    @Test
    void emptySetupHasNothingMountedAndNoChanges() {
        MotorbikeSetup setup = emptySetup();

        for (SetupComponent component : SetupComponent.values()) {
            assertThat(setup.current(component)).isEmpty();
            assertThat(setup.changeCount(component)).isZero();
            assertThat(setup.history(component)).isEmpty();
        }
    }

    @Test
    void mountingClosesPreviousEntryTheDayBefore() {
        MotorbikeSetup setup = emptySetup();
        SetupEntry previous = setup.mount(SetupComponent.REAR_TYRE, SUPERCORSA, SEP_02);

        SetupEntry mounted = setup.mount(SetupComponent.REAR_TYRE, POWER_CUP, OCT_03);

        assertThat(previous.getRemovedOn()).isEqualTo(LocalDate.of(2026, 10, 2));
        assertThat(previous.isCurrent()).isFalse();
        assertThat(setup.current(SetupComponent.REAR_TYRE)).contains(mounted);
        assertThat(setup.changeCount(SetupComponent.REAR_TYRE)).isEqualTo(2);
        assertThat(setup.history(SetupComponent.REAR_TYRE)).containsExactly(mounted, previous);
    }

    @Test
    void sameTyreModelCanBeMountedAgain() {
        MotorbikeSetup setup = emptySetup();
        setup.mount(SetupComponent.REAR_TYRE, POWER_CUP, SEP_02);

        setup.mount(SetupComponent.REAR_TYRE, POWER_CUP, SEP_19);

        assertThat(setup.changeCount(SetupComponent.REAR_TYRE)).isEqualTo(2);
    }

    @Test
    void changingOneComponentLeavesTheOthersUntouched() {
        MotorbikeSetup setup = emptySetup();
        SetupEntry front = setup.mount(SetupComponent.FRONT_TYRE, POWER_CUP, SEP_02);

        setup.mount(SetupComponent.REAR_TYRE, POWER_CUP, OCT_03);

        assertThat(front.isCurrent()).isTrue();
        assertThat(setup.current(SetupComponent.FRONT_TYRE)).contains(front);
    }

    @Test
    void mountingOnOrBeforeCurrentMountedDateIsRejected() {
        MotorbikeSetup setup = emptySetup();
        SetupEntry current = setup.mount(SetupComponent.REAR_TYRE, POWER_CUP, SEP_19);

        assertThatThrownBy(() -> setup.mount(SetupComponent.REAR_TYRE, SUPERCORSA, SEP_19))
            .isInstanceOf(SetupChangeDateConflictException.class);
        assertThatThrownBy(() -> setup.mount(SetupComponent.REAR_TYRE, SUPERCORSA, SEP_02))
            .isInstanceOf(SetupChangeDateConflictException.class);

        assertThat(current.isCurrent()).isTrue();
        assertThat(setup.changeCount(SetupComponent.REAR_TYRE)).isEqualTo(1);
    }

    @Test
    void mountingInTheFutureIsRejected() {
        assertThatThrownBy(() -> emptySetup().mount(SetupComponent.FRONT_TYRE, POWER_CUP, LocalDate.now().plusDays(1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Mounted date cannot be in the future");
    }

    @Test
    void specMustMatchComponent() {
        MotorbikeSetup setup = emptySetup();

        assertThatThrownBy(() -> setup.mount(SetupComponent.FINAL_DRIVE, POWER_CUP, SEP_02))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> setup.mount(SetupComponent.FRONT_TYRE, new FinalDriveRatio(16, 48), SEP_02))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failedMountDoesNotClosePreviousEntry() {
        MotorbikeSetup setup = emptySetup();
        SetupEntry current = setup.mount(SetupComponent.FINAL_DRIVE, new FinalDriveRatio(16, 48), SEP_02);

        assertThatThrownBy(() -> setup.mount(SetupComponent.FINAL_DRIVE, POWER_CUP, OCT_03))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(current.isCurrent()).isTrue();
    }

    @Test
    void correctingMountedDateReclosesPreviousEntry() {
        MotorbikeSetup setup = emptySetup();
        SetupEntry previous = setup.mount(SetupComponent.REAR_TYRE, SUPERCORSA, SEP_02);
        SetupEntry current = setup.mount(SetupComponent.REAR_TYRE, POWER_CUP, OCT_03);

        setup.correct(current.getId(), new TyreSpec("Michelin Power Cup 2", "190/55 ZR17"), SEP_19);

        assertThat(current.getMountedOn()).isEqualTo(SEP_19);
        assertThat(current.getSpec()).isEqualTo(new TyreSpec("Michelin Power Cup 2", "190/55 ZR17"));
        assertThat(previous.getRemovedOn()).isEqualTo(LocalDate.of(2026, 9, 18));
    }

    @Test
    void correctionCannotJumpOverNeighbourEntries() {
        MotorbikeSetup setup = emptySetup();
        SetupEntry first = setup.mount(SetupComponent.REAR_TYRE, SUPERCORSA, SEP_02);
        SetupEntry middle = setup.mount(SetupComponent.REAR_TYRE, POWER_CUP, SEP_19);
        setup.mount(SetupComponent.REAR_TYRE, POWER_CUP, OCT_03);

        assertThatThrownBy(() -> setup.correct(middle.getId(), POWER_CUP, SEP_02))
            .isInstanceOf(SetupChangeDateConflictException.class);
        assertThatThrownBy(() -> setup.correct(middle.getId(), POWER_CUP, OCT_03))
            .isInstanceOf(SetupChangeDateConflictException.class);
        assertThatThrownBy(() -> setup.correct(first.getId(), SUPERCORSA, SEP_19))
            .isInstanceOf(SetupChangeDateConflictException.class);

        assertThat(middle.getMountedOn()).isEqualTo(SEP_19);
    }

    @Test
    void correctingUnknownEntryFails() {
        assertThatThrownBy(() -> emptySetup().correct(SetupEntryId.newId(), POWER_CUP, SEP_02))
            .isInstanceOf(SetupEntryNotFoundException.class);
    }

    @Test
    void tyreSpecTrimsTextAndTreatsBlankSizeAsMissing() {
        TyreSpec spec = new TyreSpec("  Michelin Power Cup 2 ", "  ");

        assertThat(spec.model()).isEqualTo("Michelin Power Cup 2");
        assertThat(spec.size()).isNull();
        assertThatThrownBy(() -> new TyreSpec(" ", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void finalDriveRatioValidatesTeethAndComputesRatio() {
        assertThat(new FinalDriveRatio(16, 48).ratio()).isEqualByComparingTo("3.000");
        assertThatThrownBy(() -> new FinalDriveRatio(0, 48)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FinalDriveRatio(16, 100)).isInstanceOf(IllegalArgumentException.class);
    }
}
