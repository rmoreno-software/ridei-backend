package com.ridei.garage.application;

import java.time.LocalDate;

import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.SetupEntryId;
import com.ridei.garage.domain.model.SetupSpec;

public record CorrectSetupEntryCommand(
    OwnerId ownerId,
    MotorbikeId motorbikeId,
    SetupEntryId entryId,
    SetupSpec spec,
    LocalDate mountedOn
) {}
