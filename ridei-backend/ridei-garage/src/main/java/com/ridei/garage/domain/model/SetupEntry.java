package com.ridei.garage.domain.model;

import java.time.Instant;
import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Un periodo del histórico de un componente: qué estuvo montado, desde cuándo y hasta cuándo.
 * {@code removedOn == null} significa que sigue montado (entrada actual).
 * Solo el agregado {@link MotorbikeSetup} puede modificarla.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SetupEntry {

    private final SetupEntryId id;
    private final SetupComponent component;
    private SetupSpec spec;
    private LocalDate mountedOn;
    private LocalDate removedOn;
    private final Instant createdAt;

    static SetupEntry mount(SetupComponent component, SetupSpec spec, LocalDate mountedOn) {
        if (component == null) throw new IllegalArgumentException("Setup component is required");
        validate(component, spec, mountedOn);
        return new SetupEntry(SetupEntryId.newId(), component, spec, mountedOn, null, Instant.now());
    }

    public static SetupEntry reconstitute(
        SetupEntryId id,
        SetupComponent component,
        SetupSpec spec,
        LocalDate mountedOn,
        LocalDate removedOn,
        Instant createdAt
    ) {
        return new SetupEntry(id, component, spec, mountedOn, removedOn, createdAt);
    }

    public boolean isCurrent() {
        return removedOn == null;
    }

    void correct(SetupSpec spec, LocalDate mountedOn) {
        validate(component, spec, mountedOn);
        this.spec = spec;
        this.mountedOn = mountedOn;
    }

    void closeOn(LocalDate removedOn) {
        this.removedOn = removedOn;
    }

    private static void validate(SetupComponent component, SetupSpec spec, LocalDate mountedOn) {
        if (spec == null)
            throw new IllegalArgumentException("Setup specification is required");
        if (!component.accepts(spec))
            throw new IllegalArgumentException("Specification does not match component " + component);
        if (mountedOn == null)
            throw new IllegalArgumentException("Mounted date is required");
        if (mountedOn.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Mounted date cannot be in the future");
    }
}
