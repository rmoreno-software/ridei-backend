package com.ridei.circuits.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.ridei.circuits.domain.exception.MergedCatalogItemException;

/**
 * Trazado concreto de un {@link Track}: por dónde se va exactamente. Entidad interna del
 * agregado {@link Venue}; solo se modifica a través de él.
 */
public final class Layout {

    /** Dos layouts cuya longitud difiere como mucho esto (y con mismas curvas y sentido) son el mismo. */
    public static final int EQUIVALENT_LENGTH_TOLERANCE_METERS = 25;

    private final LayoutId id;
    private LayoutName name;
    private Length length;
    private Turns turns;
    private Direction direction;
    private boolean isDefault;
    private CatalogStatus status;
    private LayoutId mergedInto;
    private final Instant createdAt;

    private Layout(
        LayoutId id,
        LayoutName name,
        Length length,
        Turns turns,
        Direction direction,
        boolean isDefault,
        CatalogStatus status,
        LayoutId mergedInto,
        Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.length = length;
        this.turns = turns;
        this.direction = direction;
        this.isDefault = isDefault;
        this.status = status;
        this.mergedInto = mergedInto;
        this.createdAt = createdAt;
    }

    static Layout create(LayoutSpec spec, Instant now) {
        return new Layout(
            LayoutId.newId(), spec.name(), spec.length(), spec.turns(), spec.direction(),
            false, CatalogStatus.PENDING_REVIEW, null, now
        );
    }

    public static Layout reconstitute(
        LayoutId id,
        LayoutName name,
        Length length,
        Turns turns,
        Direction direction,
        boolean isDefault,
        CatalogStatus status,
        LayoutId mergedInto,
        Instant createdAt
    ) {
        if (id == null || name == null || length == null || direction == null || status == null || createdAt == null) {
            throw new IllegalArgumentException("Layout id, name, length, direction, status and creation date are required");
        }
        return new Layout(id, name, length, turns, direction, isDefault, status, mergedInto, createdAt);
    }

    /**
     * Es equivalente si el nombre es similar, o si la longitud está dentro de
     * {@link #EQUIVALENT_LENGTH_TOLERANCE_METERS} y coinciden curvas y sentido.
     */
    public boolean isEquivalentTo(LayoutSpec other) {
        return name.isSimilarTo(other.name()) || hasSameGeometryAs(other);
    }

    public boolean isEquivalentTo(Layout other) {
        return isEquivalentTo(other.spec());
    }

    private boolean hasSameGeometryAs(LayoutSpec other) {
        return length.isWithinToleranceOf(other.length(), EQUIVALENT_LENGTH_TOLERANCE_METERS)
            && Objects.equals(turns, other.turns())
            && direction == other.direction();
    }

    public LayoutSpec spec() {
        return new LayoutSpec(name, length, turns, direction);
    }

    public boolean isActive() {
        return status.isActive();
    }

    void markDefault() {
        requireActive();
        isDefault = true;
    }

    void unmarkDefault() {
        isDefault = false;
    }

    void verify() {
        requireActive();
        status = CatalogStatus.VERIFIED;
    }

    void correct(LayoutSpec corrected) {
        requireActive();
        name = corrected.name();
        length = corrected.length();
        turns = corrected.turns();
        direction = corrected.direction();
    }

    void markMergedInto(LayoutId target) {
        requireActive();
        status = CatalogStatus.MERGED;
        mergedInto = target;
        isDefault = false;
    }

    void requireActive() {
        if (!isActive()) throw new MergedCatalogItemException("Layout", id.value(), mergedInto.value());
    }

    public LayoutId getId() {
        return id;
    }

    public LayoutName getName() {
        return name;
    }

    public Length getLength() {
        return length;
    }

    /** Número de curvas, o {@code null} si se desconoce. */
    public Turns getTurns() {
        return turns;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public CatalogStatus getStatus() {
        return status;
    }

    /** Layout canónico si este está {@code MERGED}; si no, {@code null}. */
    public LayoutId getMergedInto() {
        return mergedInto;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
