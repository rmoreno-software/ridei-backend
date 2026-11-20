package com.ridei.circuits.infrastructure.adapter.in.rest;

import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.montmelo;
import static com.ridei.circuits.domain.model.CatalogFixtures.onlyTrack;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.LayoutCorrection;
import com.ridei.circuits.domain.model.LayoutPath;
import com.ridei.circuits.domain.model.Venue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** Contrato JSON del campo {@code path}: presente, {@code null} y ausente; y su validación. */
class LayoutPathDtoTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final ValidatorFactory VALIDATION = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATION.getValidator();

    private static final List<GeoPoint> TRIANGLE = List.of(
        GeoPoint.of(41.5700, 2.2611), GeoPoint.of(41.5703, 2.2618), GeoPoint.of(41.5690, 2.2620));

    @AfterAll
    static void close() {
        VALIDATION.close();
    }

    @Nested
    class Serialization {

        @Test
        void pathPresentIsAnArrayOfLatitudeLongitudeInLapOrder() throws Exception {
            Venue venue = montmelo();
            Layout gp = onlyTrack(venue).defaultLayout().orElseThrow();
            venue.correctLayout(gp.getId(), new LayoutCorrection(null, null, null, null, LayoutPath.of(TRIANGLE)), NOW);

            JsonNode json = JSON.valueToTree(LayoutResponseDTO.fromDomain(gp));

            assertThat(json.get("path").isArray()).isTrue();
            assertThat(json.get("path")).hasSize(3);
            assertThat(json.get("path").get(0).get("latitude").asDouble()).isEqualTo(41.5700);
            assertThat(json.get("path").get(0).get("longitude").asDouble()).isEqualTo(2.2611);
            assertThat(json.get("path").get(1).get("latitude").asDouble()).isEqualTo(41.5703);
            assertThat(json.get("path").get(0).fieldNames()).toIterable().containsExactly("latitude", "longitude");
        }

        @Test
        void layoutWithoutDrawingIsSerializedAsNull() throws Exception {
            JsonNode json = JSON.valueToTree(LayoutResponseDTO.fromDomain(onlyTrack(montmelo()).defaultLayout().orElseThrow()));

            assertThat(json.has("path")).isTrue();
            assertThat(json.get("path").isNull()).isTrue();
        }

        @Test
        void absentPathInARequestMeansDoNotChangeIt() throws Exception {
            UpdateLayoutRequestDTO request = JSON.readValue("{\"name\": \"GP\"}", UpdateLayoutRequestDTO.class);

            assertThat(request.path()).isNull();
            assertThat(request.toCorrection().path()).isNull();
        }

        @Test
        void requestPathIsSimplifiedAndLosesTheRepeatedClosingPoint() throws Exception {
            UpdateLayoutRequestDTO request = JSON.readValue("""
                {"path": [ {"latitude": 41.5700, "longitude": 2.2611}, {"latitude": 41.5703, "longitude": 2.2618},
                           {"latitude": 41.5690, "longitude": 2.2620}, {"latitude": 41.5700, "longitude": 2.2611} ]}
                """, UpdateLayoutRequestDTO.class);

            assertThat(request.toCorrection().path().points()).containsExactlyElementsOf(TRIANGLE);
        }
    }

    @Nested
    class Validation_ {

        @Test
        void pointOutOfRangeIsRejected() {
            UpdateLayoutRequestDTO request = withPath(List.of(
                new PathPointDTO(41.57, 2.26), new PathPointDTO(41.57, 180.01), new PathPointDTO(41.56, 2.27)));

            assertThat(messages(VALIDATOR.validate(request))).containsExactly("Path point longitude must be between -180 and 180");
        }

        @Test
        void nullCoordinateIsRejected() {
            UpdateLayoutRequestDTO request = withPath(List.of(
                new PathPointDTO(41.57, 2.26), new PathPointDTO(null, 2.27), new PathPointDTO(41.56, 2.27)));

            assertThat(messages(VALIDATOR.validate(request))).containsExactly("Path point latitude is required");
        }

        @Test
        void lessThanThreePointsIsRejected() {
            UpdateLayoutRequestDTO request = withPath(List.of(new PathPointDTO(41.57, 2.26), new PathPointDTO(41.56, 2.27)));

            assertThat(messages(VALIDATOR.validate(request))).containsExactly("Layout path must have between 3 and 20000 points");
        }

        @Test
        void validPathPasses() {
            UpdateLayoutRequestDTO request = withPath(TRIANGLE.stream().map(PathPointDTO::fromDomain).toList());

            assertThat(VALIDATOR.validate(request)).isEmpty();
        }

        private static UpdateLayoutRequestDTO withPath(List<PathPointDTO> path) {
            return new UpdateLayoutRequestDTO(null, null, null, null, path);
        }

        private static List<String> messages(Set<ConstraintViolation<UpdateLayoutRequestDTO>> violations) {
            return violations.stream().map(ConstraintViolation::getMessage).toList();
        }
    }
}
