package com.ridei.sessions.infrastructure.adapter.out.circuits;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ridei.sessions.domain.exception.ExternalServiceUnavailableException;
import com.ridei.sessions.domain.exception.LayoutNotTimeableException;
import com.ridei.sessions.domain.model.Gate;
import com.ridei.sessions.domain.model.GeoPoint;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.LayoutSnapshot;
import com.ridei.sessions.domain.model.TrackGeometry;
import com.ridei.sessions.domain.model.VenueId;
import com.ridei.sessions.domain.port.out.LayoutCatalogPort;
import com.ridei.sessions.infrastructure.adapter.out.http.CurrentBearerToken;
import com.ridei.sessions.infrastructure.adapter.out.http.UpstreamUnauthorizedException;

/** {@code GET /api/v1/circuits/venues/{venueId}} de ridei-circuits, con el token del piloto. */
public class CircuitsCatalogAdapter implements LayoutCatalogPort {

    private final RestClient circuits;

    public CircuitsCatalogAdapter(RestClient circuits) {
        this.circuits = circuits;
    }

    @Override
    public Optional<LayoutSnapshot> findLayout(VenueId venueId, LayoutId layoutId) {
        VenueDetail detail;
        try {
            detail = circuits.get()
                .uri("/api/v1/circuits/venues/{venueId}", venueId.value())
                .header(HttpHeaders.AUTHORIZATION, CurrentBearerToken.authorizationHeader())
                .retrieve()
                .body(VenueDetail.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) return Optional.empty();
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) throw new UpstreamUnauthorizedException();
            throw new ExternalServiceUnavailableException("The circuits catalog", e);
        } catch (RestClientException e) {
            throw new ExternalServiceUnavailableException("The circuits catalog", e);
        }
        if (detail == null || detail.venue() == null || detail.venue().tracks() == null) return Optional.empty();

        Venue venue = detail.venue();
        for (Track track : venue.tracks()) {
            if (track.layouts() == null) continue;
            for (Layout layout : track.layouts()) {
                if (!layoutId.value().equals(layout.id())) continue;
                return Optional.of(new LayoutSnapshot(
                    VenueId.of(venue.id()), venue.name(), track.name(), layoutId, layout.name(), geometry(layout)));
            }
        }
        return Optional.empty();
    }

    private static TrackGeometry geometry(Layout layout) {
        if (layout.finishLine() == null || layout.finishLine().size() != 2 || layout.path() == null || layout.path().size() < 3) {
            throw new LayoutNotTimeableException();
        }
        Gate finish = Gate.of(layout.finishLine().get(0).toDomain(), layout.finishLine().get(1).toDomain());
        return TrackGeometry.of(finish, layout.path().stream().map(Point::toDomain).toList());
    }

    // Solo los campos que usamos de la respuesta de circuits.

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VenueDetail(Venue venue) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Venue(UUID id, String name, List<Track> tracks) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Track(String name, List<Layout> layouts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Layout(UUID id, String name, List<Point> path, List<Point> finishLine) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Point(double latitude, double longitude) {
        GeoPoint toDomain() {
            return GeoPoint.of(latitude, longitude);
        }
    }
}
