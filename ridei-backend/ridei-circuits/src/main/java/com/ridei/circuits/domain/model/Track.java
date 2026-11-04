package com.ridei.circuits.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.ridei.circuits.domain.exception.DuplicateLayoutException;
import com.ridei.circuits.domain.exception.InvalidMergeException;
import com.ridei.circuits.domain.exception.LayoutNotFoundException;
import com.ridei.circuits.domain.exception.MergedCatalogItemException;

/**
 * Pista de un venue para una disciplina. Entidad interna del agregado {@link Venue}.
 *
 * <p>Invariante: si tiene layouts activos, exactamente uno es el default. El primero que se
 * crea pasa a serlo.
 */
public final class Track {

    private final TrackId id;
    private Discipline discipline;
    private TrackName name;
    private CatalogStatus status;
    private TrackId mergedInto;
    private final Instant createdAt;
    private final List<Layout> layouts;

    private Track(
        TrackId id,
        Discipline discipline,
        TrackName name,
        CatalogStatus status,
        TrackId mergedInto,
        Instant createdAt,
        List<Layout> layouts
    ) {
        this.id = id;
        this.discipline = discipline;
        this.name = name;
        this.status = status;
        this.mergedInto = mergedInto;
        this.createdAt = createdAt;
        this.layouts = layouts;
    }

    static Track create(TrackSpec spec, Instant now) {
        return new Track(
            TrackId.newId(), spec.discipline(), spec.name(), CatalogStatus.PENDING_REVIEW, null, now, new ArrayList<>()
        );
    }

    public static Track reconstitute(
        TrackId id,
        Discipline discipline,
        TrackName name,
        CatalogStatus status,
        TrackId mergedInto,
        Instant createdAt,
        List<Layout> layouts
    ) {
        if (id == null || discipline == null || name == null || status == null || createdAt == null) {
            throw new IllegalArgumentException("Track id, discipline, name, status and creation date are required");
        }
        return new Track(id, discipline, name, status, mergedInto, createdAt,
            new ArrayList<>(layouts == null ? List.of() : layouts));
    }

    /** Mismo venue + misma disciplina + nombre similar ⇒ es el mismo track. */
    public boolean isDuplicateOf(TrackSpec candidate) {
        return isActive() && discipline == candidate.discipline() && name.isSimilarTo(candidate.name());
    }

    Layout addLayout(LayoutSpec spec, Instant now) {
        requireActive();
        rejectEquivalentLayout(spec, null);
        Layout layout = Layout.create(spec, now);
        adopt(layout);
        return layout;
    }

    void setDefaultLayout(LayoutId layoutId) {
        requireActive();
        Layout target = activeLayout(layoutId);
        layouts.forEach(Layout::unmarkDefault);
        target.markDefault();
        ensureSingleDefault();
    }

    void verifyLayout(LayoutId layoutId) {
        layout(layoutId).verify();
    }

    void correctLayout(LayoutId layoutId, LayoutCorrection correction) {
        requireActive();
        Layout layout = activeLayout(layoutId);
        LayoutSpec corrected = correction.applyTo(layout.spec());
        rejectEquivalentLayout(corrected, layoutId);
        layout.correct(corrected);
    }

    /** Fusiona dos layouts de este track. Si el origen era el default, el destino pasa a serlo. */
    void mergeLayouts(LayoutId sourceId, LayoutId targetId) {
        requireActive();
        if (sourceId.equals(targetId)) throw new InvalidMergeException("A layout cannot be merged into itself");
        Layout source = activeLayout(sourceId);
        Layout target = activeLayout(targetId);
        boolean sourceWasDefault = source.isDefault();
        source.markMergedInto(target.getId());
        if (sourceWasDefault) target.markDefault();
        ensureSingleDefault();
    }

    /**
     * Incorpora los layouts de {@code source} (duplicado de este track): los equivalentes se
     * marcan {@code MERGED} y los demás se mueven aquí sin ser default. {@code source} queda
     * {@code MERGED} apuntando a este track.
     */
    TrackAbsorption absorb(Track source) {
        requireActive();
        source.requireActive();
        if (source.id.equals(id)) throw new InvalidMergeException("A track cannot be merged into itself");
        if (source.discipline != discipline) throw new InvalidMergeException("Tracks of different disciplines cannot be merged");

        List<LayoutId> moved = new ArrayList<>();
        List<TrackAbsorption.MergedLayout> merged = new ArrayList<>();
        for (Layout layout : source.activeLayouts()) {
            Optional<Layout> equivalent = findEquivalentLayout(layout.spec(), null);
            if (equivalent.isPresent()) {
                layout.markMergedInto(equivalent.get().getId());
                merged.add(new TrackAbsorption.MergedLayout(layout.getId(), equivalent.get().getId()));
            } else {
                source.layouts.remove(layout);
                layout.unmarkDefault();
                adopt(layout);
                moved.add(layout.getId());
            }
        }
        source.markMergedInto(id);
        ensureSingleDefault();
        return new TrackAbsorption(moved, merged);
    }

    void verify() {
        requireActive();
        status = CatalogStatus.VERIFIED;
    }

    void correct(TrackSpec corrected) {
        requireActive();
        discipline = corrected.discipline();
        name = corrected.name();
    }

    void markMergedInto(TrackId target) {
        requireActive();
        status = CatalogStatus.MERGED;
        mergedInto = target;
    }

    void requireActive() {
        if (!isActive()) throw new MergedCatalogItemException("Track", id.value(), mergedInto.value());
    }

    private void adopt(Layout layout) {
        if (activeLayouts().isEmpty()) layout.markDefault();
        layouts.add(layout);
        ensureSingleDefault();
    }

    private void rejectEquivalentLayout(LayoutSpec spec, LayoutId ignoring) {
        findEquivalentLayout(spec, ignoring).ifPresent(existing -> {
            throw new DuplicateLayoutException(existing, existing.getName().similarityTo(spec.name()));
        });
    }

    private Optional<Layout> findEquivalentLayout(LayoutSpec spec, LayoutId ignoring) {
        return activeLayouts().stream()
            .filter(layout -> !layout.getId().equals(ignoring))
            .filter(layout -> layout.isEquivalentTo(spec))
            .findFirst();
    }

    private void ensureSingleDefault() {
        List<Layout> active = activeLayouts();
        long defaults = active.stream().filter(Layout::isDefault).count();
        if (!active.isEmpty() && defaults != 1) {
            throw new IllegalStateException("Track " + id + " must have exactly one default layout, found " + defaults);
        }
    }

    public boolean isActive() {
        return status.isActive();
    }

    public TrackSpec spec() {
        return new TrackSpec(discipline, name);
    }

    public boolean hasLayout(LayoutId layoutId) {
        return layouts.stream().anyMatch(layout -> layout.getId().equals(layoutId));
    }

    public Layout layout(LayoutId layoutId) {
        return layouts.stream()
            .filter(layout -> layout.getId().equals(layoutId))
            .findFirst()
            .orElseThrow(() -> new LayoutNotFoundException(layoutId));
    }

    private Layout activeLayout(LayoutId layoutId) {
        Layout layout = layout(layoutId);
        layout.requireActive();
        return layout;
    }

    public List<Layout> activeLayouts() {
        return layouts.stream().filter(Layout::isActive).toList();
    }

    public Optional<Layout> defaultLayout() {
        return layouts.stream().filter(Layout::isActive).filter(Layout::isDefault).findFirst();
    }

    public TrackId getId() {
        return id;
    }

    public Discipline getDiscipline() {
        return discipline;
    }

    public TrackName getName() {
        return name;
    }

    public CatalogStatus getStatus() {
        return status;
    }

    /** Track canónico si este está {@code MERGED}; si no, {@code null}. */
    public TrackId getMergedInto() {
        return mergedInto;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Todos los layouts, incluidos los {@code MERGED}. */
    public List<Layout> getLayouts() {
        return Collections.unmodifiableList(layouts);
    }
}
