package com.ridei.garage.infrastructure.adapter.in.rest;

import java.time.LocalDate;

import com.ridei.garage.application.CorrectSetupEntryCommand;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.SetupEntryId;
import com.ridei.garage.domain.model.SetupSpec;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

/**
 * Corrección de una entrada del histórico ("Editar" en la pantalla 11B).
 * Reemplaza la especificación y la fecha de montaje: se envían completas.
 * Debe venir {@code tyre} para neumáticos o {@code finalDrive} para la relación, no ambos.
 */
@Data
public class CorrectSetupEntryRequestDTO {

    @NotNull(message = "Mounted date is required")
    @PastOrPresent(message = "Mounted date cannot be in the future")
    private LocalDate mountedOn;

    @Valid
    private TyreSpecRequestDTO tyre;

    @Valid
    private FinalDriveRequestDTO finalDrive;

    @AssertTrue(message = "Provide either tyre or finalDrive, not both")
    private boolean isSingleSpecPresent() {
        return (tyre != null) ^ (finalDrive != null);
    }

    public CorrectSetupEntryCommand toCommand(OwnerId ownerId, MotorbikeId motorbikeId, SetupEntryId entryId) {
        SetupSpec spec = tyre != null ? tyre.toDomain() : finalDrive.toDomain();
        return new CorrectSetupEntryCommand(ownerId, motorbikeId, entryId, spec, mountedOn);
    }
}
