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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * Administración del catálogo; {@code SecurityConfig} exige rol {@code ADMIN} en todo
 * {@code /admin/**}. Verify y merge de tracks/layouts usan ids planos porque un admin que
 * detecta un duplicado no siempre sabe a qué venue pertenece.
 */
@Tag(name = "Admin", description = "Administración del catálogo. Requiere un token con rol ADMIN.")
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

    @Operation(summary = "Verify a venue",
        description = "PENDING_REVIEW → VERIFIED. Returns the venue.")
    @PostMapping("/venues/{venueId}/verify")
    public VenueResponseDTO verifyVenue(@PathVariable String venueId) {
        return VenueResponseDTO.fromDomain(verifyVenueUseCase.verify(VenueId.of(venueId)));
    }

    @Operation(summary = "Verify a track",
        description = "Returns the venue that contains it.")
    @PostMapping("/tracks/{trackId}/verify")
    public VenueResponseDTO verifyTrack(@PathVariable String trackId) {
        return VenueResponseDTO.fromDomain(verifyTrackUseCase.verify(TrackId.of(trackId)));
    }

    @Operation(summary = "Verify a layout",
        description = "Returns the venue that contains it.")
    @PostMapping("/layouts/{layoutId}/verify")
    public VenueResponseDTO verifyLayout(@PathVariable String layoutId) {
        return VenueResponseDTO.fromDomain(verifyLayoutUseCase.verify(LayoutId.of(layoutId)));
    }

    // ── Fusiones (devuelven el venue destino) ───────────────────────────────────────────

    @Operation(summary = "Merge duplicate venues",
        description = "Moves every track of source to target (clashing tracks and layouts are merged too). source becomes MERGED. Returns the target venue.")
    @PostMapping("/venues/{sourceId}/merge-into/{targetId}")
    public VenueResponseDTO mergeVenues(@PathVariable String sourceId, @PathVariable String targetId) {
        return VenueResponseDTO.fromDomain(
            mergeVenuesUseCase.merge(new MergeVenuesCommand(VenueId.of(sourceId), VenueId.of(targetId))));
    }

    @Operation(summary = "Merge duplicate tracks",
        description = "Same discipline only; may belong to different venues. Returns the target's venue.")
    @PostMapping("/tracks/{sourceId}/merge-into/{targetId}")
    public VenueResponseDTO mergeTracks(@PathVariable String sourceId, @PathVariable String targetId) {
        return VenueResponseDTO.fromDomain(
            mergeTracksUseCase.merge(new MergeTracksCommand(TrackId.of(sourceId), TrackId.of(targetId))));
    }

    @Operation(summary = "Merge duplicate layouts",
        description = "Same track only. If source was the default, target becomes the default.")
    @PostMapping("/layouts/{sourceId}/merge-into/{targetId}")
    public VenueResponseDTO mergeLayouts(@PathVariable String sourceId, @PathVariable String targetId) {
        return VenueResponseDTO.fromDomain(
            mergeLayoutsUseCase.merge(new MergeLayoutsCommand(LayoutId.of(sourceId), LayoutId.of(targetId))));
    }

    // ── Layout por defecto y correcciones ───────────────────────────────────────────────

    @Operation(summary = "Set the default layout",
        description = "Returns the track.")
    @PutMapping("/venues/{venueId}/tracks/{trackId}/default-layout/{layoutId}")
    public TrackResponseDTO setDefaultLayout(
        @PathVariable String venueId,
        @PathVariable String trackId,
        @PathVariable String layoutId
    ) {
        return TrackResponseDTO.fromDomain(setDefaultLayoutUseCase.setDefault(
            new SetDefaultLayoutCommand(VenueId.of(venueId), TrackId.of(trackId), LayoutId.of(layoutId))));
    }

    @Operation(summary = "Correct a venue",
        description = "Only the fields sent are changed.")
    @PatchMapping("/venues/{venueId}")
    public VenueResponseDTO updateVenue(@PathVariable String venueId, @RequestBody @Valid UpdateVenueRequestDTO dto) {
        return VenueResponseDTO.fromDomain(
            updateVenueUseCase.update(new UpdateVenueCommand(VenueId.of(venueId), dto.toCorrection())));
    }

    @Operation(summary = "Correct a track",
        description = "Only the fields sent are changed; duplicate rules apply.")
    @PatchMapping("/venues/{venueId}/tracks/{trackId}")
    public TrackResponseDTO updateTrack(
        @PathVariable String venueId,
        @PathVariable String trackId,
        @RequestBody @Valid UpdateTrackRequestDTO dto
    ) {
        return TrackResponseDTO.fromDomain(updateTrackUseCase.update(
            new UpdateTrackCommand(VenueId.of(venueId), TrackId.of(trackId), dto.toCorrection())));
    }

    @Operation(summary = "Correct a layout",
        description = "Only the fields sent are changed; equivalence rules apply.")
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
