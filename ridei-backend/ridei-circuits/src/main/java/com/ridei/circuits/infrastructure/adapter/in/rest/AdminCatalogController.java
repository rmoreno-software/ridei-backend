package com.ridei.circuits.infrastructure.adapter.in.rest;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.circuits.application.MergeLayoutsCommand;
import com.ridei.circuits.application.MergeTracksCommand;
import com.ridei.circuits.application.MergeVenuesCommand;
import com.ridei.circuits.application.SetDefaultLayoutCommand;
import com.ridei.circuits.application.UpdateLayoutCommand;
import com.ridei.circuits.application.UpdateTrackCommand;
import com.ridei.circuits.application.UpdateVenueCommand;
import com.ridei.circuits.application.port.in.MergeLayoutsUseCase;
import com.ridei.circuits.application.port.in.MergeTracksUseCase;
import com.ridei.circuits.application.port.in.MergeVenuesUseCase;
import com.ridei.circuits.application.port.in.SetDefaultLayoutUseCase;
import com.ridei.circuits.application.port.in.UpdateLayoutUseCase;
import com.ridei.circuits.application.port.in.UpdateTrackUseCase;
import com.ridei.circuits.application.port.in.UpdateVenueUseCase;
import com.ridei.circuits.application.port.in.VerifyLayoutUseCase;
import com.ridei.circuits.application.port.in.VerifyTrackUseCase;
import com.ridei.circuits.application.port.in.VerifyVenueUseCase;
import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.VenueId;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * Administración del catálogo; {@code SecurityConfig} exige rol {@code ADMIN} en todo
 * {@code /admin/**}. Verify y merge de tracks/layouts usan ids planos porque un admin que
 * detecta un duplicado no siempre sabe a qué venue pertenece.
 */
@RestController
@RequestMapping("/api/v1/circuits/admin")
@AllArgsConstructor
public class AdminCatalogController {

    private final VerifyVenueUseCase verifyVenueUseCase;
    private final VerifyTrackUseCase verifyTrackUseCase;
    private final VerifyLayoutUseCase verifyLayoutUseCase;
    private final MergeVenuesUseCase mergeVenuesUseCase;
    private final MergeTracksUseCase mergeTracksUseCase;
    private final MergeLayoutsUseCase mergeLayoutsUseCase;
    private final SetDefaultLayoutUseCase setDefaultLayoutUseCase;
    private final UpdateVenueUseCase updateVenueUseCase;
    private final UpdateTrackUseCase updateTrackUseCase;
    private final UpdateLayoutUseCase updateLayoutUseCase;

    // ── Verificación ────────────────────────────────────────────────────────────────────

    @PostMapping("/venues/{venueId}/verify")
    public VenueResponseDTO verifyVenue(@PathVariable String venueId) {
        return VenueResponseDTO.fromDomain(verifyVenueUseCase.verify(VenueId.of(venueId)));
    }

    @PostMapping("/tracks/{trackId}/verify")
    public VenueResponseDTO verifyTrack(@PathVariable String trackId) {
        return VenueResponseDTO.fromDomain(verifyTrackUseCase.verify(TrackId.of(trackId)));
    }

    @PostMapping("/layouts/{layoutId}/verify")
    public VenueResponseDTO verifyLayout(@PathVariable String layoutId) {
        return VenueResponseDTO.fromDomain(verifyLayoutUseCase.verify(LayoutId.of(layoutId)));
    }

    // ── Fusiones (devuelven el venue destino) ───────────────────────────────────────────

    @PostMapping("/venues/{sourceId}/merge-into/{targetId}")
    public VenueResponseDTO mergeVenues(@PathVariable String sourceId, @PathVariable String targetId) {
        return VenueResponseDTO.fromDomain(
            mergeVenuesUseCase.merge(new MergeVenuesCommand(VenueId.of(sourceId), VenueId.of(targetId))));
    }

    @PostMapping("/tracks/{sourceId}/merge-into/{targetId}")
    public VenueResponseDTO mergeTracks(@PathVariable String sourceId, @PathVariable String targetId) {
        return VenueResponseDTO.fromDomain(
            mergeTracksUseCase.merge(new MergeTracksCommand(TrackId.of(sourceId), TrackId.of(targetId))));
    }

    @PostMapping("/layouts/{sourceId}/merge-into/{targetId}")
    public VenueResponseDTO mergeLayouts(@PathVariable String sourceId, @PathVariable String targetId) {
        return VenueResponseDTO.fromDomain(
            mergeLayoutsUseCase.merge(new MergeLayoutsCommand(LayoutId.of(sourceId), LayoutId.of(targetId))));
    }

    // ── Layout por defecto y correcciones ───────────────────────────────────────────────

    @PutMapping("/venues/{venueId}/tracks/{trackId}/default-layout/{layoutId}")
    public TrackResponseDTO setDefaultLayout(
        @PathVariable String venueId,
        @PathVariable String trackId,
        @PathVariable String layoutId
    ) {
        return TrackResponseDTO.fromDomain(setDefaultLayoutUseCase.setDefault(
            new SetDefaultLayoutCommand(VenueId.of(venueId), TrackId.of(trackId), LayoutId.of(layoutId))));
    }

    @PatchMapping("/venues/{venueId}")
    public VenueResponseDTO updateVenue(@PathVariable String venueId, @RequestBody @Valid UpdateVenueRequestDTO dto) {
        return VenueResponseDTO.fromDomain(
            updateVenueUseCase.update(new UpdateVenueCommand(VenueId.of(venueId), dto.toCorrection())));
    }

    @PatchMapping("/venues/{venueId}/tracks/{trackId}")
    public TrackResponseDTO updateTrack(
        @PathVariable String venueId,
        @PathVariable String trackId,
        @RequestBody @Valid UpdateTrackRequestDTO dto
    ) {
        return TrackResponseDTO.fromDomain(updateTrackUseCase.update(
            new UpdateTrackCommand(VenueId.of(venueId), TrackId.of(trackId), dto.toCorrection())));
    }

    @PatchMapping("/venues/{venueId}/tracks/{trackId}/layouts/{layoutId}")
    public LayoutResponseDTO updateLayout(
        @PathVariable String venueId,
        @PathVariable String trackId,
        @PathVariable String layoutId,
        @RequestBody @Valid UpdateLayoutRequestDTO dto
    ) {
        return LayoutResponseDTO.fromDomain(updateLayoutUseCase.update(new UpdateLayoutCommand(
            VenueId.of(venueId), TrackId.of(trackId), LayoutId.of(layoutId), dto.toCorrection())));
    }
}
