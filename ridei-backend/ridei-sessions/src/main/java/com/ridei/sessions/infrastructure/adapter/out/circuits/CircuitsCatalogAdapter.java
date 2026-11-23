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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ridei.sessions.domain.exception.ExternalServiceUnavailableException;
import com.ridei.sessions.domain.exception.LayoutNotTimeableException;
import com.ridei.sessions.domain.model.CatalogVenue;
import com.ridei.sessions.domain.model.Gate;
import com.ridei.sessions.domain.model.GeoPoint;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.LayoutSnapshot;
import com.ridei.sessions.domain.model.TrackGeometry;
import com.ridei.sessions.domain.model.VenueId;
import com.ridei.sessions.domain.port.out.LayoutCatalogPort;
import com.ridei.sessions.domain.port.out.VenueCatalogPort;
import com.ridei.sessions.infrastructure.adapter.out.http.CurrentBearerToken;
import com.ridei.sessions.infrastructure.adapter.out.http.UpstreamUnauthorizedException;

/**
 * {@code GET /api/v1/circuits/venues/{venueId}} y {@code GET /api/v1/circuits/venues?q=} de
 * ridei-circuits, con el token del piloto.
 */
public class CircuitsCatalogAdapter implements LayoutCatalogPort, VenueCatalogPort {

    private final RestClient circuits;

    public CircuitsCatalogAdapter(RestClient circuits) {
        this.circuits = circuits;
    }

    @Override
    public Optional<LayoutSnapshot> findLayout(VenueId venueId, LayoutId layoutId) {
        VenueDetail detail = fetchVenue(venueId);
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

    @Override
    public Optional<CatalogVenue> findVenue(VenueId venueId) {
        VenueDetail detail = fetchVenue(venueId);
        if (detail == null || detail.venue() == null) return Optional.empty();
        return Optional.of(detail.venue().toCatalog());
    }

    @Override
    public List<CatalogVenue> search(String text, int limit) {
        SearchPage page;
        try {
            page = circuits.get()
                .uri(b -> b.path("/api/v1/circuits/venues").queryParam("q", text).queryParam("size", limit).build())
                .header(HttpHeaders.AUTHORIZATION, CurrentBearerToken.authorizationHeader())
                .retrieve()
                .body(SearchPage.class);
        } catch (HttpClientErrorException e) {
            // 400: texto que circuits no acepta (p. ej. demasiado largo); como no encontrar nada.
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) return List.of();
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) throw new UpstreamUnauthorizedException();
            throw new ExternalServiceUnavailableException("The circuits catalog", e);
        } catch (RestClientException e) {
            throw new ExternalServiceUnavailableException("The circuits catalog", e);
        }
        if (page == null || page.content() == null) return List.of();
        return page.content().stream()
            .map(SearchItem::venue)
            .filter(v -> v != null && v.id() != null && v.name() != null)
            .map(Venue::toCatalog)
            .toList();
    }

    /** @return {@code null} si el complejo no existe */
    private VenueDetail fetchVenue(VenueId venueId) {
        try {
            return circuits.get()
                .uri("/api/v1/circuits/venues/{venueId}", venueId.value())
                .header(HttpHeaders.AUTHORIZATION, CurrentBearerToken.authorizationHeader())
                .retrieve()
                .body(VenueDetail.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) return null;
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) throw new UpstreamUnauthorizedException();
            throw new ExternalServiceUnavailableException("The circuits catalog", e);
        } catch (RestClientException e) {
            throw new ExternalServiceUnavailableException("The circuits catalog", e);
        }
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
    record SearchPage(List<SearchItem> content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchItem(Venue venue) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Venue(UUID id, String name, List<Track> tracks) {
        CatalogVenue toCatalog() {
            List<CatalogVenue.Track> catalogTracks = tracks == null ? List.of() : tracks.stream()
                .map(t -> new CatalogVenue.Track(t.name(), t.discipline(), t.layouts() == null ? List.of() : t.layouts().stream()
                    .filter(l -> l.id() != null && l.name() != null)
                    .map(l -> new CatalogVenue.Layout(LayoutId.of(l.id()), l.name(), l.isDefault()))
                    .toList()))
                .toList();
            return new CatalogVenue(VenueId.of(id), name, catalogTracks);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Track(String name, String discipline, List<Layout> layouts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Layout(UUID id, String name, @JsonProperty("isDefault") boolean isDefault, List<Point> path, List<Point> finishLine) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Point(double latitude, double longitude) {
        GeoPoint toDomain() {
            return GeoPoint.of(latitude, longitude);
        }
    }
}
