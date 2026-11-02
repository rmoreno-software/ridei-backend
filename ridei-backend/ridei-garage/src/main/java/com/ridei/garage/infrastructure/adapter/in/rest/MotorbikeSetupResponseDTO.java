package com.ridei.garage.infrastructure.adapter.in.rest;

import com.ridei.garage.domain.model.MotorbikeSetup;
import com.ridei.garage.domain.model.SetupComponent;

/**
 * Setup actual de la moto (bloque "SETUP · CON HISTÓRICO" de la ficha, pantallas 11 y 11G).
 * Los tres componentes vienen siempre; {@code current} es null si aún no hay nada registrado.
 */
public record MotorbikeSetupResponseDTO(
    String motorbikeId,
    ComponentSummary frontTyre,
    ComponentSummary rearTyre,
    ComponentSummary finalDrive
) {
    public record ComponentSummary(SetupEntryResponseDTO current, int changeCount) {

        static ComponentSummary of(MotorbikeSetup setup, SetupComponent component) {
            return new ComponentSummary(
                setup.current(component).map(SetupEntryResponseDTO::fromDomain).orElse(null),
                setup.changeCount(component)
            );
        }
    }

    public static MotorbikeSetupResponseDTO fromDomain(MotorbikeSetup setup) {
        return new MotorbikeSetupResponseDTO(
            setup.getMotorbikeId().value().toString(),
            ComponentSummary.of(setup, SetupComponent.FRONT_TYRE),
            ComponentSummary.of(setup, SetupComponent.REAR_TYRE),
            ComponentSummary.of(setup, SetupComponent.FINAL_DRIVE)
        );
    }
}
