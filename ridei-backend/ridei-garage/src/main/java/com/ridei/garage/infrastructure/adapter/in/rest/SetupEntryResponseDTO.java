package com.ridei.garage.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ridei.garage.domain.model.FinalDriveRatio;
import com.ridei.garage.domain.model.SetupEntry;
import com.ridei.garage.domain.model.TyreSpec;

/** Una entrada del histórico. Solo uno de {@code tyre} / {@code finalDrive} viene informado. */
public record SetupEntryResponseDTO(
    String id,
    String component,
    Tyre tyre,
    FinalDrive finalDrive,
    LocalDate mountedOn,
    LocalDate removedOn,
    boolean current
) {
    public record Tyre(String model, String size) {}

    public record FinalDrive(int frontSprocket, int rearSprocket, BigDecimal ratio) {}

    public static SetupEntryResponseDTO fromDomain(SetupEntry entry) {
        Tyre tyre = null;
        FinalDrive finalDrive = null;
        switch (entry.getSpec()) {
            case TyreSpec t -> tyre = new Tyre(t.model(), t.size());
            case FinalDriveRatio d -> finalDrive = new FinalDrive(d.frontSprocket(), d.rearSprocket(), d.ratio());
        }
        return new SetupEntryResponseDTO(
            entry.getId().value().toString(),
            entry.getComponent().name(),
            tyre,
            finalDrive,
            entry.getMountedOn(),
            entry.getRemovedOn(),
            entry.isCurrent()
        );
    }
}
