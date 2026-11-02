package com.ridei.garage.application;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.SetupComponent;
import com.ridei.garage.domain.model.SetupSpec;

/**
 * Uno o varios componentes que se cambian el mismo día ("Los dos" neumáticos, por ejemplo).
 * Los componentes que no aparecen no cambian.
 */
public record RegisterSetupChangeCommand(
    OwnerId ownerId,
    MotorbikeId motorbikeId,
    LocalDate mountedOn,
    Map<SetupComponent, SetupSpec> changes
) {
    public RegisterSetupChangeCommand {
        if (changes == null || changes.isEmpty())
            throw new IllegalArgumentException("At least one setup change is required");
        // EnumMap: orden de aplicación determinista
        changes = Collections.unmodifiableMap(new EnumMap<>(changes));
    }
}
