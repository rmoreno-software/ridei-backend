package com.ridei.circuits.infrastructure.adapter.in.rest;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.circuits.application.AddLayoutToTrackCommand;
import com.ridei.circuits.application.AddTrackToVenueCommand;
import com.ridei.circuits.application.ProposeVenueResult;
import com.ridei.circuits.application.port.in.AddLayoutToTrackUseCase;
import com.ridei.circuits.application.port.in.AddTrackToVenueUseCase;
import com.ridei.circuits.application.port.in.FindNearbyVenuesUseCase;
import com.ridei.circuits.application.port.in.GetVenueDetailUseCase;
import com.ridei.circuits.application.port.in.ProposeVenueUseCase;
import com.ridei.circuits.application.port.in.SearchVenuesUseCase;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.UserId;
import com.ridei.circuits.domain.model.VenueId;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/** Consultas y propuestas de cualquier usuario autenticado. */
@RestController
@RequestMapping(VenueController.BASE_PATH)
@AllArgsConstructor
public class VenueController {

    static final String BASE_PATH = "/api/v1/circuits/venues";

    private final SearchVenuesUseCase searchVenuesUseCase;
    private final FindNearbyVenuesUseCase findNearbyVenuesUseCase;
    private final GetVenueDetailUseCase getVenueDetailUseCase;
    private final ProposeVenueUseCase proposeVenueUseCase;
    private final AddTrackToVenueUseCase addTrackToVenueUseCase;
    private final AddLayoutToTrackUseCase addLayoutToTrackUseCase;

    @GetMapping
    public PageResponseDTO<VenueSearchItemDTO> search(@Valid SearchVenuesRequestDTO params) {
        return PageResponseDTO.fromDomain(searchVenuesUseCase.search(params.toQuery()), VenueSearchItemDTO::fromDomain);
    }

    @GetMapping("/nearby")
    public PageResponseDTO<VenueSearchItemDTO> nearby(@Valid NearbyVenuesRequestDTO params) {
        return PageResponseDTO.fromDomain(findNearbyVenuesUseCase.findNearby(params.toQuery()), VenueSearchItemDTO::fromDomain);
    }

    @GetMapping("/{venueId}")
    public VenueDetailResponseDTO get(@PathVariable String venueId) {
        return VenueDetailResponseDTO.fromDetail(getVenueDetailUseCase.get(VenueId.of(venueId)));
    }

    /** 201 si se crea; 200 si ya existía uno con el mismo {@code externalPlaceId}; 409 si hay posibles duplicados. */
    @PostMapping
    public ResponseEntity<ProposeVenueResponseDTO> propose(
        @RequestBody @Valid ProposeVenueRequestDTO dto,
        Authentication authentication
    ) {
        ProposeVenueResult result = proposeVenueUseCase.propose(dto.toCommand(UserId.of(authentication.getName())));
        ProposeVenueResponseDTO body = ProposeVenueResponseDTO.fromResult(result);
        if (!result.isCreated()) return ResponseEntity.ok(body);
        return ResponseEntity.created(URI.create(BASE_PATH + "/" + result.venue().getId())).body(body);
    }

    @PostMapping("/{venueId}/tracks")
    public ResponseEntity<TrackResponseDTO> addTrack(
        @PathVariable String venueId,
        @RequestBody @Valid TrackRequestDTO dto
    ) {
        Track track = addTrackToVenueUseCase.addTrack(new AddTrackToVenueCommand(VenueId.of(venueId), dto.toSpec()));
        return ResponseEntity.status(HttpStatus.CREATED).body(TrackResponseDTO.fromDomain(track));
    }

    @PostMapping("/{venueId}/tracks/{trackId}/layouts")
    public ResponseEntity<LayoutResponseDTO> addLayout(
        @PathVariable String venueId,
        @PathVariable String trackId,
        @RequestBody @Valid LayoutRequestDTO dto
    ) {
        Layout layout = addLayoutToTrackUseCase.addLayout(
            new AddLayoutToTrackCommand(VenueId.of(venueId), TrackId.of(trackId), dto.toSpec()));
        return ResponseEntity.status(HttpStatus.CREATED).body(LayoutResponseDTO.fromDomain(layout));
    }
}
