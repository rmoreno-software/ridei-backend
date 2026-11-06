package com.ridei.circuits.infrastructure.adapter.in.rest;

import static com.ridei.circuits.domain.model.CatalogFixtures.MONTMELO;
import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.alcarras;
import static com.ridei.circuits.domain.model.CatalogFixtures.layout;
import static com.ridei.circuits.domain.model.CatalogFixtures.montmelo;
import static com.ridei.circuits.domain.model.CatalogFixtures.onlyTrack;
import static com.ridei.circuits.domain.model.CatalogFixtures.pendingLayout;
import static com.ridei.circuits.domain.model.CatalogFixtures.track;
import static com.ridei.circuits.domain.model.CatalogFixtures.venue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.ridei.circuits.application.AddLayoutToTrackCommand;
import com.ridei.circuits.application.ProposeVenueCommand;
import com.ridei.circuits.application.ProposeVenueResult;
import com.ridei.circuits.application.VenueDetail;
import com.ridei.circuits.application.port.in.AddLayoutToTrackUseCase;
import com.ridei.circuits.application.port.in.AddTrackToVenueUseCase;
import com.ridei.circuits.application.port.in.FindNearbyVenuesUseCase;
import com.ridei.circuits.application.port.in.GetVenueDetailUseCase;
import com.ridei.circuits.application.port.in.MergeLayoutsUseCase;
import com.ridei.circuits.application.port.in.MergeTracksUseCase;
import com.ridei.circuits.application.port.in.MergeVenuesUseCase;
import com.ridei.circuits.application.port.in.ProposeVenueUseCase;
import com.ridei.circuits.application.port.in.SearchVenuesUseCase;
import com.ridei.circuits.application.port.in.SetDefaultLayoutUseCase;
import com.ridei.circuits.application.port.in.UpdateLayoutUseCase;
import com.ridei.circuits.application.port.in.UpdateTrackUseCase;
import com.ridei.circuits.application.port.in.UpdateVenueUseCase;
import com.ridei.circuits.application.port.in.VerifyLayoutUseCase;
import com.ridei.circuits.application.port.in.VerifyTrackUseCase;
import com.ridei.circuits.application.port.in.VerifyVenueUseCase;
import com.ridei.circuits.domain.exception.CatalogItemAlreadyExistsException;
import com.ridei.circuits.domain.exception.DuplicateLayoutException;
import com.ridei.circuits.domain.exception.DuplicateTrackException;
import com.ridei.circuits.domain.exception.PossibleDuplicateVenueException;
import com.ridei.circuits.domain.exception.VenueNotFoundException;
import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.Distance;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.UserId;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueCandidate;
import com.ridei.circuits.domain.model.VenueId;
import com.ridei.circuits.infrastructure.adapter.out.identity.TokenValidationStatus;
import com.ridei.circuits.infrastructure.adapter.out.identity.TokenValidator;
import com.ridei.circuits.infrastructure.config.SecurityConfig;

@WebMvcTest(controllers = {VenueController.class, AdminCatalogController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class CatalogApiTest {

    private static final String VENUES = "/api/v1/circuits/venues";
    private static final String ADMIN = "/api/v1/circuits/admin";

    private static final String PROPOSAL = """
        {
          "name": "Circuit de Barcelona-Catalunya",
          "countryCode": "ES",
          "latitude": 41.57,
          "longitude": 2.2611,
          "externalPlaceId": "ChIJ-montmelo",
          "createdBy": "11111111-1111-4111-8111-111111111111",
          "track": { "discipline": "ROAD_RACING", "name": "Velocidad" },
          "layout": { "name": "GP", "lengthMeters": 4657, "turns": 14, "direction": "CLOCKWISE" }
        }
        """;

    @DynamicPropertySource
    static void jwtKey(DynamicPropertyRegistry registry) {
        registry.add("jwt.public-key", TestTokens::publicKeyBase64);
    }

    @Autowired MockMvc mvc;

    @MockBean SearchVenuesUseCase searchVenues;
    @MockBean FindNearbyVenuesUseCase findNearbyVenues;
    @MockBean GetVenueDetailUseCase getVenueDetail;
    @MockBean ProposeVenueUseCase proposeVenue;
    @MockBean AddTrackToVenueUseCase addTrack;
    @MockBean AddLayoutToTrackUseCase addLayout;
    @MockBean VerifyVenueUseCase verifyVenue;
    @MockBean VerifyTrackUseCase verifyTrack;
    @MockBean VerifyLayoutUseCase verifyLayout;
    @MockBean MergeVenuesUseCase mergeVenues;
    @MockBean MergeTracksUseCase mergeTracks;
    @MockBean MergeLayoutsUseCase mergeLayouts;
    @MockBean SetDefaultLayoutUseCase setDefaultLayout;
    @MockBean UpdateVenueUseCase updateVenue;
    @MockBean UpdateTrackUseCase updateTrack;
    @MockBean UpdateLayoutUseCase updateLayout;
    @MockBean TokenValidator tokenValidator;

    @BeforeEach
    void identityConsidersTokensValid() {
        when(tokenValidator.validate(any())).thenReturn(TokenValidationStatus.VALID);
    }

    @Nested
    class Security {

        @Test
        void requestsWithoutTokenAreUnauthorized() throws Exception {
            mvc.perform(get(VENUES + "/" + VenueId.newId())).andExpect(status().isUnauthorized());
        }

        @Test
        void refreshTokensAreNotAccepted() throws Exception {
            mvc.perform(get(VENUES + "/" + VenueId.newId()).header(HttpHeaders.AUTHORIZATION, TestTokens.refreshToken()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void revokedTokensAreUnauthorized() throws Exception {
            when(tokenValidator.validate(any())).thenReturn(TokenValidationStatus.REVOKED);

            mvc.perform(get(VENUES + "/" + VenueId.newId()).header(HttpHeaders.AUTHORIZATION, TestTokens.rider()))
                .andExpect(status().isUnauthorized());
            verifyNoInteractions(getVenueDetail);
        }

        @Test
        void identityDownFailsClosedWith503() throws Exception {
            when(tokenValidator.validate(any())).thenReturn(TokenValidationStatus.UNAVAILABLE);

            mvc.perform(get(VENUES + "/" + VenueId.newId()).header(HttpHeaders.AUTHORIZATION, TestTokens.rider()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.timestamp").isString());
            verifyNoInteractions(getVenueDetail);
        }

        @Test
        void tokensWithABadSignatureNeverReachIdentity() throws Exception {
            mvc.perform(get(VENUES + "/" + VenueId.newId()).header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
            verifyNoInteractions(tokenValidator);
        }

        @Test
        void ridersCannotUseAdminCommands() throws Exception {
            mvc.perform(post(ADMIN + "/venues/" + VenueId.newId() + "/verify")
                    .header(HttpHeaders.AUTHORIZATION, TestTokens.rider()))
                .andExpect(status().isForbidden());
            verifyNoInteractions(verifyVenue);
        }

        @Test
        void adminsCanVerify() throws Exception {
            Venue venue = montmelo();
            venue.verify(NOW);
            when(verifyVenue.verify(venue.getId())).thenReturn(venue);

            mvc.perform(post(ADMIN + "/venues/" + venue.getId() + "/verify")
                    .header(HttpHeaders.AUTHORIZATION, TestTokens.admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
        }

        @Test
        void adminsCanMergeVenues() throws Exception {
            Venue target = montmelo();
            Venue source = venue("Montmeló", MONTMELO, track(Discipline.KARTING, "Karting"));
            when(mergeVenues.merge(any())).thenReturn(target);

            mvc.perform(post(ADMIN + "/venues/" + source.getId() + "/merge-into/" + target.getId())
                    .header(HttpHeaders.AUTHORIZATION, TestTokens.admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(target.getId().toString()));
        }
    }

    @Nested
    class ProposeVenue {

        @Test
        void createdReturns201WithLocationAndTakesTheProposerFromTheToken() throws Exception {
            Venue venue = montmelo();
            when(proposeVenue.propose(any())).thenReturn(ProposeVenueResult.created(venue));

            mvc.perform(post(VENUES).header(HttpHeaders.AUTHORIZATION, TestTokens.rider())
                    .contentType(MediaType.APPLICATION_JSON).content(PROPOSAL))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, VENUES + "/" + venue.getId()))
                .andExpect(jsonPath("$.outcome").value("CREATED"))
                .andExpect(jsonPath("$.venue.tracks[0].layouts[0].isDefault").value(true));

            ArgumentCaptor<ProposeVenueCommand> command = ArgumentCaptor.forClass(ProposeVenueCommand.class);
            verify(proposeVenue).propose(command.capture());
            assertThat(command.getValue().proposedBy()).isEqualTo(new UserId(TestTokens.RIDER_ID));
            assertThat(command.getValue().confirmedNotDuplicate()).isFalse();
        }

        @Test
        void existingByExternalPlaceIdReturns200AndSaysSo() throws Exception {
            when(proposeVenue.propose(any())).thenReturn(ProposeVenueResult.existing(montmelo()));

            mvc.perform(post(VENUES).header(HttpHeaders.AUTHORIZATION, TestTokens.rider())
                    .contentType(MediaType.APPLICATION_JSON).content(PROPOSAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("EXISTING_BY_EXTERNAL_PLACE_ID"));
        }

        @Test
        void possibleDuplicateReturns409WithCandidates() throws Exception {
            Venue existing = montmelo();
            VenueCandidate candidate = new VenueCandidate(existing.getId(), existing.getName(), CatalogStatus.VERIFIED,
                Distance.ofMeters(812.4), 0.7333);
            when(proposeVenue.propose(any())).thenThrow(new PossibleDuplicateVenueException(List.of(candidate)));

            mvc.perform(post(VENUES).header(HttpHeaders.AUTHORIZATION, TestTokens.rider())
                    .contentType(MediaType.APPLICATION_JSON).content(PROPOSAL))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.code").value("POSSIBLE_DUPLICATE_VENUE"))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.candidates[0].id").value(existing.getId().toString()))
                .andExpect(jsonPath("$.candidates[0].name").value("Circuit de Barcelona-Catalunya"))
                .andExpect(jsonPath("$.candidates[0].distanceMeters").value(812))
                .andExpect(jsonPath("$.candidates[0].similarity").value(0.733))
                .andExpect(jsonPath("$.candidates[0].status").value("VERIFIED"));
        }

        @Test
        void lostRaceIsAConflictNeverAServerError() throws Exception {
            when(proposeVenue.propose(any())).thenThrow(new CatalogItemAlreadyExistsException("unique", null));

            mvc.perform(post(VENUES).header(HttpHeaders.AUTHORIZATION, TestTokens.rider())
                    .contentType(MediaType.APPLICATION_JSON).content(PROPOSAL))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATALOG_ITEM_ALREADY_EXISTS"));
        }

        @Test
        void invalidInputIs400WithDetails() throws Exception {
            String invalid = """
                { "name": "", "countryCode": "34", "latitude": 95, "longitude": 2, "track": null }
                """;

            mvc.perform(post(VENUES).header(HttpHeaders.AUTHORIZATION, TestTokens.rider())
                    .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.length()").value(4));
            verifyNoInteractions(proposeVenue);
        }
    }

    @Nested
    class TracksAndLayouts {

        @Test
        void duplicateTrackReturns409WithTheExistingTrack() throws Exception {
            Venue venue = alcarras();
            Track velocidad = onlyTrack(venue);
            when(addTrack.addTrack(any())).thenThrow(new DuplicateTrackException(velocidad, 1.0));

            mvc.perform(post(VENUES + "/" + venue.getId() + "/tracks").header(HttpHeaders.AUTHORIZATION, TestTokens.rider())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"discipline\": \"ROAD_RACING\", \"name\": \"Velocidad\" }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TRACK"))
                .andExpect(jsonPath("$.candidates[0].id").value(velocidad.getId().toString()))
                .andExpect(jsonPath("$.candidates[0].distanceMeters").doesNotExist());
        }

        @Test
        void newLayoutReturns201() throws Exception {
            Venue venue = montmelo();
            Track road = onlyTrack(venue);
            Layout chicane = venue.addLayout(road.getId(), layout("GP con chicane", 4675, 16, Direction.CLOCKWISE), NOW);
            when(addLayout.addLayout(any(AddLayoutToTrackCommand.class))).thenReturn(chicane);

            mvc.perform(post(VENUES + "/" + venue.getId() + "/tracks/" + road.getId() + "/layouts")
                    .header(HttpHeaders.AUTHORIZATION, TestTokens.rider())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"name\": \"GP con chicane\", \"lengthMeters\": 4675, \"turns\": 16, \"direction\": \"CLOCKWISE\" }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isDefault").value(false));
        }

        @Test
        void layoutWithOnlyANameReturns201WithoutGeometry() throws Exception {
            Venue venue = montmelo();
            Track road = onlyTrack(venue);
            Layout pending = venue.addLayout(road.getId(), pendingLayout("Variante 2"), NOW);
            when(addLayout.addLayout(any(AddLayoutToTrackCommand.class))).thenReturn(pending);

            mvc.perform(post(VENUES + "/" + venue.getId() + "/tracks/" + road.getId() + "/layouts")
                    .header(HttpHeaders.AUTHORIZATION, TestTokens.rider())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"name\": \"Variante 2\" }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Variante 2"))
                .andExpect(jsonPath("$.lengthMeters").doesNotExist())
                .andExpect(jsonPath("$.direction").doesNotExist());
        }

        @Test
        void equivalentLayoutReturns409WithTheExistingLayout() throws Exception {
            Venue venue = montmelo();
            Track road = onlyTrack(venue);
            Layout gp = road.defaultLayout().orElseThrow();
            when(addLayout.addLayout(any())).thenThrow(new DuplicateLayoutException(gp, 0.0));

            mvc.perform(post(VENUES + "/" + venue.getId() + "/tracks/" + road.getId() + "/layouts")
                    .header(HttpHeaders.AUTHORIZATION, TestTokens.rider())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"name\": \"Variante F1\", \"lengthMeters\": 4670, \"turns\": 14, \"direction\": \"CLOCKWISE\" }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_LAYOUT"))
                .andExpect(jsonPath("$.candidates[0].id").value(gp.getId().toString()));
        }
    }

    @Nested
    class Queries {

        @Test
        void mergedVenueIdReturnsTheCanonicalOne() throws Exception {
            Venue canonical = montmelo();
            VenueId mergedId = VenueId.newId();
            when(getVenueDetail.get(mergedId)).thenReturn(new VenueDetail(mergedId, canonical));

            mvc.perform(get(VENUES + "/" + mergedId).header(HttpHeaders.AUTHORIZATION, TestTokens.rider()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedId").value(mergedId.toString()))
                .andExpect(jsonPath("$.redirectedFromMerged").value(true))
                .andExpect(jsonPath("$.venue.id").value(canonical.getId().toString()));
        }

        @Test
        void unknownVenueIs404AndMalformedIdIs400() throws Exception {
            VenueId unknown = VenueId.newId();
            when(getVenueDetail.get(unknown)).thenThrow(new VenueNotFoundException(unknown));

            mvc.perform(get(VENUES + "/" + unknown).header(HttpHeaders.AUTHORIZATION, TestTokens.rider()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
            mvc.perform(get(VENUES + "/not-a-uuid").header(HttpHeaders.AUTHORIZATION, TestTokens.rider()))
                .andExpect(status().isBadRequest());
        }

        @Test
        void proximityNeedsLatLonAndRadiusTogether() throws Exception {
            mvc.perform(get(VENUES).param("lat", "41.5").header(HttpHeaders.AUTHORIZATION, TestTokens.rider()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("lat, lon and radiusKm must be given together"));
            verifyNoInteractions(searchVenues);
        }
    }
}
