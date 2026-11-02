package com.ridei.garage.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.ridei.garage.domain.exception.SetupChangeDateConflictException;
import com.ridei.garage.domain.exception.SetupEntryNotFoundException;

import lombok.Getter;

/**
 * Agregado con el setup de una moto y su histórico por componente (neumático delantero,
 * neumático trasero y relación final). Es independiente del agregado {@link Motorbike}:
 * el histórico crece con cada cambio y no debe cargarse para editar los datos de la moto.
 *
 * <p>Invariantes, por componente:
 * <ul>
 *   <li>Los periodos forman una cadena continua y sin solapes, ordenada por {@code mountedOn}.</li>
 *   <li>Hay como mucho una entrada actual (sin {@code removedOn}), y es la más reciente.</li>
 *   <li>Al montar una pieza nueva, la anterior se cierra el día previo al montaje.</li>
 * </ul>
 */
public class MotorbikeSetup {

    private static final Comparator<SetupEntry> CHRONOLOGICAL = Comparator.comparing(SetupEntry::getMountedOn);

    @Getter
    private final MotorbikeId motorbikeId;
    private final List<SetupEntry> entries;

    private MotorbikeSetup(MotorbikeId motorbikeId, List<SetupEntry> entries) {
        if (motorbikeId == null) throw new IllegalArgumentException("Motorbike id is required");
        this.motorbikeId = motorbikeId;
        this.entries = new ArrayList<>(entries);
    }

    public static MotorbikeSetup reconstitute(MotorbikeId motorbikeId, List<SetupEntry> entries) {
        return new MotorbikeSetup(motorbikeId, entries);
    }

    // ---- Comandos -------------------------------------------------------

    /**
     * Registra que se monta {@code spec} en {@code component} el día {@code mountedOn}.
     * Si había algo montado, pasa al histórico con fin el día anterior.
     */
    public SetupEntry mount(SetupComponent component, SetupSpec spec, LocalDate mountedOn) {
        Optional<SetupEntry> current = current(component);
        current.ifPresent(previous -> requireAfter(mountedOn, previous));

        SetupEntry mounted = SetupEntry.mount(component, spec, mountedOn);
        current.ifPresent(previous -> previous.closeOn(mountedOn.minusDays(1)));
        entries.add(mounted);
        return mounted;
    }

    /**
     * Corrige una entrada ya registrada (errata en el modelo, la medida o la fecha de montaje).
     * La nueva fecha debe quedar entre la entrada anterior y la siguiente; la anterior se
     * vuelve a cerrar para que la cadena siga siendo continua.
     */
    public SetupEntry correct(SetupEntryId entryId, SetupSpec spec, LocalDate mountedOn) {
        SetupEntry target = find(entryId);
        List<SetupEntry> chain = chronological(target.getComponent());
        int index = chain.indexOf(target);
        Optional<SetupEntry> previous = index > 0 ? Optional.of(chain.get(index - 1)) : Optional.empty();
        Optional<SetupEntry> next = index < chain.size() - 1 ? Optional.of(chain.get(index + 1)) : Optional.empty();

        previous.ifPresent(p -> requireAfter(mountedOn, p));
        next.ifPresent(n -> requireBefore(mountedOn, n));

        target.correct(spec, mountedOn);
        previous.ifPresent(p -> p.closeOn(mountedOn.minusDays(1)));
        return target;
    }

    // ---- Consultas ------------------------------------------------------

    public Optional<SetupEntry> current(SetupComponent component) {
        return entries.stream()
            .filter(e -> e.getComponent() == component && e.isCurrent())
            .findFirst();
    }

    /** Histórico de un componente, del más reciente al más antiguo. */
    public List<SetupEntry> history(SetupComponent component) {
        return chronological(component).reversed();
    }

    /** Número de piezas registradas en el componente, incluida la actual. */
    public int changeCount(SetupComponent component) {
        return (int) entries.stream().filter(e -> e.getComponent() == component).count();
    }

    public List<SetupEntry> getEntries() {
        return List.copyOf(entries);
    }

    // ---- Internos -------------------------------------------------------

    private List<SetupEntry> chronological(SetupComponent component) {
        return entries.stream()
            .filter(e -> e.getComponent() == component)
            .sorted(CHRONOLOGICAL)
            .toList();
    }

    private SetupEntry find(SetupEntryId entryId) {
        return entries.stream()
            .filter(e -> e.getId().equals(entryId))
            .findFirst()
            .orElseThrow(SetupEntryNotFoundException::new);
    }

    /** Null se deja pasar: la validación de campo obligatorio la hace {@link SetupEntry}. */
    private static void requireAfter(LocalDate mountedOn, SetupEntry previous) {
        if (mountedOn != null && !mountedOn.isAfter(previous.getMountedOn()))
            throw new SetupChangeDateConflictException(
                "Mounted date must be after " + previous.getMountedOn()
                    + ", when the previous " + previous.getComponent() + " was mounted");
    }

    private static void requireBefore(LocalDate mountedOn, SetupEntry next) {
        if (mountedOn != null && !mountedOn.isBefore(next.getMountedOn()))
            throw new SetupChangeDateConflictException(
                "Mounted date must be before " + next.getMountedOn()
                    + ", when the next " + next.getComponent() + " was mounted");
    }
}
