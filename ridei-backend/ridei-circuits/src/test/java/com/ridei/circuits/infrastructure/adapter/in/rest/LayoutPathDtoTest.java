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
import com.ridei.circuits.domain.model.FinishLine;
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
            venue.correctLayout(gp.getId(), new LayoutCorrection(null, null, null, null, LayoutPath.of(TRIANGLE), null), NOW);

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
            assertThat(json.has("finishLine")).isTrue();
            assertThat(json.get("finishLine").isNull()).isTrue();
        }

        @Test
        void finishLineIsTwoPointsLeftThenRightAndThePathStartsOnIt() throws Exception {
            Venue venue = montmelo();
            Layout gp = onlyTrack(venue).defaultLayout().orElseThrow();
            List<GeoPoint> square = List.of(GeoPoint.of(41.000, 2.000), GeoPoint.of(41.001, 2.000),
                GeoPoint.of(41.001, 2.001), GeoPoint.of(41.000, 2.001));
            FinishLine finish = FinishLine.of(GeoPoint.of(41.0005, 1.9999), GeoPoint.of(41.0005, 2.0001));
            venue.correctLayout(gp.getId(), new LayoutCorrection(null, null, null, null, LayoutPath.of(square), finish), NOW);

            JsonNode json = JSON.valueToTree(LayoutResponseDTO.fromDomain(gp));

            assertThat(json.get("finishLine")).hasSize(2);
            assertThat(json.get("finishLine").get(0).get("longitude").asDouble()).isEqualTo(1.9999);
            assertThat(json.get("finishLine").get(1).get("longitude").asDouble()).isEqualTo(2.0001);
            assertThat(json.get("path").get(0).get("latitude").asDouble()).isEqualTo(41.0005);
            assertThat(json.get("path").get(0).get("longitude").asDouble()).isEqualTo(2.000);
        }

        @Test
        void absentPathInARequestMeansDoNotChangeIt() throws Exception {
            UpdateLayoutRequestDTO request = JSON.readValue("{\"name\": \"GP\"}", UpdateLayoutRequestDTO.class);

            assertThat(request.path()).isNull();
            assertThat(request.finishLine()).isNull();
            assertThat(request.toCorrection().path()).isNull();
            assertThat(request.toCorrection().finishLine()).isNull();
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
        void finishLineMustHaveExactlyTwoPoints() {
            UpdateLayoutRequestDTO request = new UpdateLayoutRequestDTO(null, null, null, null, null,
                List.of(new PathPointDTO(41.57, 2.26), new PathPointDTO(41.5701, 2.2601), new PathPointDTO(41.5702, 2.2602)));

            assertThat(messages(VALIDATOR.validate(request))).containsExactly("Finish line must have exactly 2 points");
        }

        @Test
        void finishLinePointOutOfRangeIsRejected() {
            UpdateLayoutRequestDTO request = new UpdateLayoutRequestDTO(null, null, null, null, null,
                List.of(new PathPointDTO(-90.5, 2.26), new PathPointDTO(41.5701, 2.2601)));

            assertThat(messages(VALIDATOR.validate(request))).containsExactly("Path point latitude must be between -90 and 90");
        }

        @Test
        void validPathPasses() {
            UpdateLayoutRequestDTO request = withPath(TRIANGLE.stream().map(PathPointDTO::fromDomain).toList());

            assertThat(VALIDATOR.validate(request)).isEmpty();
        }

        private static UpdateLayoutRequestDTO withPath(List<PathPointDTO> path) {
            return new UpdateLayoutRequestDTO(null, null, null, null, path, null);
        }

        private static List<String> messages(Set<ConstraintViolation<UpdateLayoutRequestDTO>> violations) {
            return violations.stream().map(ConstraintViolation::getMessage).toList();
        }
    }
}
