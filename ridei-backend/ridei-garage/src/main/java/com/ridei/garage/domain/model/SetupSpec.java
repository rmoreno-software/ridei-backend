package com.ridei.garage.domain.model;

/** Especificación de lo que hay montado en un componente del setup. */
public sealed interface SetupSpec permits TyreSpec, FinalDriveRatio {
}
