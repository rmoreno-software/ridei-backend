package com.ridei.garage.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

import com.ridei.garage.application.RegisterSetupChangeCommand;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.SetupComponent;
import com.ridei.garage.domain.model.SetupSpec;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

/**
 * Cambio de setup (pantalla 11C) o setup inicial (11G). Los componentes ausentes no cambian.
 */
@Data
public class RegisterSetupChangeRequestDTO {

    @NotNull(message = "Mounted date is required")
    @PastOrPresent(message = "Mounted date cannot be in the future")
    private LocalDate mountedOn;

    @Valid
    private TyreSpecRequestDTO frontTyre;

    @Valid
    private TyreSpecRequestDTO rearTyre;

    @Valid
    private FinalDriveRequestDTO finalDrive;

    @AssertTrue(message = "Provide at least one of frontTyre, rearTyre or finalDrive")
    private boolean isAnyComponentPresent() {
        return frontTyre != null || rearTyre != null || finalDrive != null;
    }

    public RegisterSetupChangeCommand toCommand(OwnerId ownerId, MotorbikeId motorbikeId) {
        Map<SetupComponent, SetupSpec> changes = new EnumMap<>(SetupComponent.class);
        if (frontTyre != null) changes.put(SetupComponent.FRONT_TYRE, frontTyre.toDomain());
        if (rearTyre != null) changes.put(SetupComponent.REAR_TYRE, rearTyre.toDomain());
        if (finalDrive != null) changes.put(SetupComponent.FINAL_DRIVE, finalDrive.toDomain());
        return new RegisterSetupChangeCommand(ownerId, motorbikeId, mountedOn, changes);
    }
}
