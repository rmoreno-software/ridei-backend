package com.ridei.circuits.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.ridei.circuits.domain.event.DomainEvent;
import com.ridei.circuits.domain.event.LayoutAdded;
import com.ridei.circuits.domain.event.LayoutMerged;
import com.ridei.circuits.domain.event.TrackAdded;
import com.ridei.circuits.domain.event.TrackMerged;
import com.ridei.circuits.domain.event.VenueMerged;
import com.ridei.circuits.domain.event.VenueProposed;
import com.ridei.circuits.domain.exception.DuplicateTrackException;
import com.ridei.circuits.domain.exception.InvalidMergeException;
import com.ridei.circuits.domain.exception.LayoutNotFoundException;
import com.ridei.circuits.domain.exception.MergedCatalogItemException;
import com.ridei.circuits.domain.exception.TrackNotFoundException;

/**
 * Raíz del agregado: un complejo (dónde está). Contiene sus {@link Track}s y, a través de
 * ellos, sus {@link Layout}s. Toda mutación pasa por aquí, actualiza {@code updatedAt} (lo
 * que hace subir la versión optimista) y registra los eventos de dominio correspondientes.
 */
public final class Venue {

    private final VenueId id;
    private VenueName name;
    private CountryCode countryCode;
    private GeoPoint location;
    private ExternalPlaceId externalPlaceId;
    private CatalogStatus status;
    private VenueId mergedInto;
    private final UserId createdBy;
    private final Instant createdAt;
    private Instant updatedAt;
    private final Long version;
    private final List<Track> tracks;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Venue(
        VenueId id,
        VenueName name,
        CountryCode countryCode,
        GeoPoint location,
        ExternalPlaceId externalPlaceId,
        CatalogStatus status,
        VenueId mergedInto,
        UserId createdBy,
        Instant createdAt,
        Instant updatedAt,
        Long version,
        List<Track> tracks
    ) {
        this.id = id;
        this.name = name;
        this.countryCode = countryCode;
        this.location = location;
        this.externalPlaceId = externalPlaceId;
        this.status = status;
        this.mergedInto = mergedInto;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
        this.tracks = tracks;
    }

    /**
     * Un usuario propone un venue con su primer track y, opcionalmente, su primer layout
     * (que pasa a ser el default). Entra como {@code PENDING_REVIEW}.
     */
    public static Venue propose(
        VenueName name,
        CountryCode countryCode,
        GeoPoint location,
        ExternalPlaceId externalPlaceId,
        UserId proposedBy,
        TrackSpec firstTrack,
        LayoutSpec firstLayout,
        Instant now
    ) {
        if (name == null || countryCode == null || location == null) {
            throw new IllegalArgumentException("Venue name, country and location are required");
        }
        if (proposedBy == null) throw new IllegalArgumentException("Proposer is required");
        if (firstTrack == null) throw new IllegalArgumentException("A venue needs its first track");

        Venue venue = new Venue(
            VenueId.newId(), name, countryCode, location, externalPlaceId,
            CatalogStatus.PENDING_REVIEW, null, proposedBy, now, now, null, new ArrayList<>()
        );
        venue.record(new VenueProposed(venue.id, name, countryCode, location, proposedBy, now));
        Track track = venue.addTrack(firstTrack, now);
        if (firstLayout != null) venue.addLayout(track.getId(), firstLayout, now);
        return venue;
    }

    public static Venue reconstitute(
        VenueId id,
        VenueName name,
        CountryCode countryCode,
        GeoPoint location,
        ExternalPlaceId externalPlaceId,
        CatalogStatus status,
        VenueId mergedInto,
        UserId createdBy,
        Instant createdAt,
        Instant updatedAt,
        Long version,
        List<Track> tracks
    ) {
        if (id == null || name == null || countryCode == null || location == null || status == null
            || createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Venue id, name, country, location, status and dates are required");
        }
        return new Venue(id, name, countryCode, location, externalPlaceId, status, mergedInto, createdBy,
            createdAt, updatedAt, version, new ArrayList<>(tracks == null ? List.of() : tracks));
    }

    // ── Tracks y layouts ────────────────────────────────────────────────────────────────

    /** Rechaza un track de la misma disciplina y nombre similar ({@link DuplicateTrackException}). */
    public Track addTrack(TrackSpec spec, Instant now) {
        requireActive();
        rejectDuplicateTrack(spec, null);
        Track track = Track.create(spec, now);
        tracks.add(track);
        touch(now);
        record(new TrackAdded(id, track.getId(), spec.discipline(), spec.name(), now));
        return track;
    }

    public Layout addLayout(TrackId trackId, LayoutSpec spec, Instant now) {
        requireActive();
        Layout layout = track(trackId).addLayout(spec, now);
        touch(now);
        record(new LayoutAdded(id, trackId, layout.getId(), layout.getName(), layout.isDefault(), now));
        return layout;
    }

    public void setDefaultLayout(TrackId trackId, LayoutId layoutId, Instant now) {
        requireActive();
        track(trackId).setDefaultLayout(layoutId);
        touch(now);
    }

    // ── Verificación y correcciones (admin) ─────────────────────────────────────────────

    public void verify(Instant now) {
        requireActive();
        status = CatalogStatus.VERIFIED;
        touch(now);
    }

    public void verifyTrack(TrackId trackId, Instant now) {
        track(trackId).verify();
        touch(now);
    }

    public void verifyLayout(LayoutId layoutId, Instant now) {
        trackOfLayout(layoutId).verifyLayout(layoutId);
        touch(now);
    }

    public void correct(VenueCorrection correction, Instant now) {
        requireActive();
        if (correction.name() != null) name = correction.name();
        if (correction.countryCode() != null) countryCode = correction.countryCode();
        if (correction.location() != null) location = correction.location();
        if (correction.externalPlaceId() != null) externalPlaceId = correction.externalPlaceId();
        touch(now);
    }

    public void correctTrack(TrackId trackId, TrackCorrection correction, Instant now) {
        Track track = track(trackId);
        track.requireActive();
        TrackSpec corrected = correction.applyTo(track.spec());
        rejectDuplicateTrack(corrected, trackId);
        track.correct(corrected);
        touch(now);
    }

    public void correctLayout(LayoutId layoutId, LayoutCorrection correction, Instant now) {
        trackOfLayout(layoutId).correctLayout(layoutId, correction);
        touch(now);
    }

    // ── Fusiones (admin) ────────────────────────────────────────────────────────────────

    /**
     * Este venue es un duplicado de {@code target}: todos sus tracks pasan a {@code target}
     * conservando sus ids. Los que chocan con un track existente (misma disciplina y nombre
     * similar) se fusionan con él, junto con sus layouts.
     */
    public void mergeInto(Venue target, Instant now) {
        requireActive();
        target.requireActive();
        if (target.id.equals(id)) throw new InvalidMergeException("A venue cannot be merged into itself");

        List<TrackId> moved = new ArrayList<>();
        for (Track track : List.copyOf(tracks)) {
            if (track.isActive()) {
                target.findDuplicateTrack(track.spec(), null)
                    .ifPresent(existing -> recordAbsorption(existing.absorb(track), track, existing, target, now));
            }
            tracks.remove(track);
            target.tracks.add(track);
            moved.add(track.getId());
        }
        status = CatalogStatus.MERGED;
        mergedInto = target.id;
        touch(now);
        target.touch(now);
        record(new VenueMerged(id, target.id, moved, now));
    }

    /**
     * El track {@code sourceTrackId} de este venue es un duplicado de {@code targetTrackId}
     * (de este u otro venue). Queda {@code MERGED} aquí y sus layouts se incorporan al destino.
     */
    public void mergeTrackInto(TrackId sourceTrackId, Venue targetVenue, TrackId targetTrackId, Instant now) {
        if (targetVenue != this && targetVenue.id.equals(id)) {
            throw new IllegalArgumentException("Pass the same Venue instance when merging tracks of one venue");
        }
        requireActive();
        targetVenue.requireActive();
        Track source = track(sourceTrackId);
        Track target = targetVenue.track(targetTrackId);
        recordAbsorption(target.absorb(source), source, target, targetVenue, now);
        touch(now);
        targetVenue.touch(now);
    }

    /** Fusiona dos layouts de un mismo track. */
    public void mergeLayouts(LayoutId sourceId, LayoutId targetId, Instant now) {
        requireActive();
        Track track = trackOfLayout(sourceId);
        if (!track.hasLayout(targetId)) throw new InvalidMergeException("Only layouts of the same track can be merged");
        track.mergeLayouts(sourceId, targetId);
        touch(now);
        record(new LayoutMerged(sourceId, targetId, track.getId(), now));
    }

    private void recordAbsorption(TrackAbsorption absorption, Track source, Track target, Venue targetVenue, Instant now) {
        record(new TrackMerged(source.getId(), target.getId(), targetVenue.id, absorption.movedLayouts(), now));
        absorption.mergedLayouts().forEach(merged ->
            record(new LayoutMerged(merged.source(), merged.target(), target.getId(), now)));
    }

    // ── Consultas ───────────────────────────────────────────────────────────────────────

    public Track track(TrackId trackId) {
        return findTrack(trackId).orElseThrow(() -> new TrackNotFoundException(trackId));
    }

    public Optional<Track> findTrack(TrackId trackId) {
        return tracks.stream().filter(track -> track.getId().equals(trackId)).findFirst();
    }

    public Track trackOfLayout(LayoutId layoutId) {
        return tracks.stream()
            .filter(track -> track.hasLayout(layoutId))
            .findFirst()
            .orElseThrow(() -> new LayoutNotFoundException(layoutId));
    }

    public List<Track> activeTracks() {
        return tracks.stream().filter(Track::isActive).toList();
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isNew() {
        return version == null;
    }

    /** Devuelve y vacía los eventos registrados desde la última llamada. */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    // ── Internos ────────────────────────────────────────────────────────────────────────

    private void rejectDuplicateTrack(TrackSpec spec, TrackId ignoring) {
        findDuplicateTrack(spec, ignoring).ifPresent(existing -> {
            throw new DuplicateTrackException(existing, existing.getName().similarityTo(spec.name()));
        });
    }

    private Optional<Track> findDuplicateTrack(TrackSpec spec, TrackId ignoring) {
        return tracks.stream()
            .filter(track -> !track.getId().equals(ignoring))
            .filter(track -> track.isDuplicateOf(spec))
            .findFirst();
    }

    private void requireActive() {
        if (!isActive()) throw new MergedCatalogItemException("Venue", id.value(), mergedInto.value());
    }

    private void touch(Instant now) {
        updatedAt = now;
    }

    private void record(DomainEvent event) {
        domainEvents.add(event);
    }

    // ── Getters ─────────────────────────────────────────────────────────────────────────

    public VenueId getId() {
        return id;
    }

    public VenueName getName() {
        return name;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public GeoPoint getLocation() {
        return location;
    }

    /** Id del proveedor de mapas, o {@code null}. */
    public ExternalPlaceId getExternalPlaceId() {
        return externalPlaceId;
    }

    public CatalogStatus getStatus() {
        return status;
    }

    /** Venue canónico si este está {@code MERGED}; si no, {@code null}. */
    public VenueId getMergedInto() {
        return mergedInto;
    }

    /** Usuario que lo propuso, o {@code null} para los datos iniciales del catálogo. */
    public UserId getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Versión optimista; {@code null} mientras no se ha persistido. */
    public Long getVersion() {
        return version;
    }

    /** Todos los tracks, incluidos los {@code MERGED}. */
    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }
}
