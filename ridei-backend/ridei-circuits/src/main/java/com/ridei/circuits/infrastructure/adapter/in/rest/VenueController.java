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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/** Consultas y propuestas de cualquier usuario autenticado. */
@Tag(name = "Venues", description = "Catálogo de circuitos para cualquier usuario autenticado: búsqueda y propuestas.")
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

    @Operation(summary = "Search venues",
        description = "Free text (matches words of venue and track names, accents/case/punctuation ignored), country, discipline and proximity (lat + lon + radiusKm together). All optional. Paginated (page from 0, size ≤ 100, default 20). Ordered by similarity, then distance, then name. Excludes merged venues.")
    @GetMapping
    public PageResponseDTO<VenueSearchItemDTO> search(@ParameterObject @Valid SearchVenuesRequestDTO params) {
        return PageResponseDTO.fromDomain(searchVenuesUseCase.search(params.toQuery()), VenueSearchItemDTO::fromDomain);
    }

    @Operation(summary = "Venues near a point",
        description = "Ordered by distance. radiusKm defaults to 50 (max 500).")
    @GetMapping("/nearby")
    public PageResponseDTO<VenueSearchItemDTO> nearby(@ParameterObject @Valid NearbyVenuesRequestDTO params) {
        return PageResponseDTO.fromDomain(findNearbyVenuesUseCase.findNearby(params.toQuery()), VenueSearchItemDTO::fromDomain);
    }

    @Operation(summary = "Venue detail",
        description = "Venue with its active tracks and layouts. If venueId was merged, the canonical venue is returned with redirectedFromMerged = true: replace the stored id with venue.id.")
    @GetMapping("/{venueId}")
    public VenueDetailResponseDTO get(@PathVariable String venueId) {
        return VenueDetailResponseDTO.fromDetail(getVenueDetailUseCase.get(VenueId.of(venueId)));
    }

    /** 201 si se crea; 200 si ya existía uno con el mismo {@code externalPlaceId}; 409 si hay posibles duplicados. */
    @Operation(summary = "Propose a venue",
        description = "Creates a venue with its first track and, optionally, its first layout (it becomes the default). 201 CREATED if new. 200 EXISTING_BY_EXTERNAL_PLACE_ID if a venue with the same externalPlaceId exists (always, even with confirmedNotDuplicate). 409 POSSIBLE_DUPLICATE_VENUE with candidates if a venue < 2 km away has a similar name: show them and resend with confirmedNotDuplicate = true if none matches. The proposer is taken from the token.")
    @ApiResponse(responseCode = "201", description = "Created (outcome = CREATED); Location header points to the venue")
    @ApiResponse(responseCode = "200", description = "A venue with the same externalPlaceId already existed (outcome = EXISTING_BY_EXTERNAL_PLACE_ID)")
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

    @Operation(summary = "Add a track to a venue",
        description = "409 DUPLICATE_TRACK if the venue already has a track of the same discipline with a similar name.")
    @ApiResponse(responseCode = "201", description = "Track created")
    @PostMapping("/{venueId}/tracks")
    public ResponseEntity<TrackResponseDTO> addTrack(
        @PathVariable String venueId,
        @RequestBody @Valid TrackRequestDTO dto
    ) {
        Track track = addTrackToVenueUseCase.addTrack(new AddTrackToVenueCommand(VenueId.of(venueId), dto.toSpec()));
        return ResponseEntity.status(HttpStatus.CREATED).body(TrackResponseDTO.fromDomain(track));
    }

    @Operation(summary = "Add a layout to a track",
        description = "The first layout of a track becomes its default. 409 DUPLICATE_LAYOUT if an equivalent layout exists: similar name, or length within ±25 m with the same turns and direction.")
    @ApiResponse(responseCode = "201", description = "Layout created")
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
