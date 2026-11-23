package com.ridei.sessions.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ridei.sessions.domain.model.CatalogVenue;
import com.ridei.sessions.domain.model.CatalogVenue.Layout;
import com.ridei.sessions.domain.model.CatalogVenue.Track;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.VenueId;
import com.ridei.sessions.domain.service.CatalogMatcher.Issue;

class CatalogMatcherTest {

    static final CatalogVenue CALAFAT = venue("Circuit de Calafat",
        new Track("Velocidad", "ROAD_RACING", List.of(layout("Principal", true), layout("Corto", false))));

    static final CatalogVenue MONTMELO = venue("Circuit de Barcelona-Catalunya",
        new Track("Velocidad", "ROAD_RACING", List.of(layout("GP", true), layout("Nacional", false))));

    static final CatalogVenue ALCARRAS = venue("Circuit d'Alcarràs",
        new Track("Velocidad", "ROAD_RACING", List.of(layout("Principal", true))),
        new Track("Karting", "KARTING", List.of(layout("Principal", true))),
        new Track("Motocross", "MOTOCROSS", List.of(layout("MX", true))));

    @Test
    void theOnlyTrackAndItsDefaultLayout() {
        CatalogMatcher.Match match = CatalogMatcher.match(CALAFAT, "Calafat");

        assertThat(match.isResolved()).isTrue();
        assertThat(match.track().name()).isEqualTo("Velocidad");
        assertThat(match.layout().name()).isEqualTo("Principal");
        assertThat(match.layoutRef()).get().extracting(r -> r.venueName()).isEqualTo("Circuit de Calafat");
    }

    @Test
    void aLayoutNamedInTheText() {
        CatalogMatcher.Match match = CatalogMatcher.match(MONTMELO, "Montmeló nacional");

        assertThat(match.layout().name()).isEqualTo("Nacional");
    }

    @Test
    void doesNotGuessBetweenSeveralTracks() {
        CatalogMatcher.Match match = CatalogMatcher.match(ALCARRAS, "Alcarras");

        assertThat(match.issue()).isEqualTo(Issue.TRACK_AMBIGUOUS);
        assertThat(match.layoutRef()).isEmpty();
    }

    @Test
    void aTrackNamedInTheTextIgnoringAccentsAndCase() {
        CatalogMatcher.Match match = CatalogMatcher.match(ALCARRAS, "alcarràs KARTING");

        assertThat(match.track().name()).isEqualTo("Karting");
    }

    @Test
    void severalLayoutsWithoutDefault() {
        CatalogVenue venue = venue("Pista X", new Track("Velocidad", "ROAD_RACING", List.of(layout("A", false), layout("B", false))));

        assertThat(CatalogMatcher.match(venue, "Pista X").issue()).isEqualTo(Issue.LAYOUT_AMBIGUOUS);
        assertThat(CatalogMatcher.match(venue("Vacío"), "Vacío").issue()).isEqualTo(Issue.NO_LAYOUTS);
    }

    static CatalogVenue venue(String name, Track... tracks) {
        return new CatalogVenue(VenueId.of(UUID.randomUUID()), name, List.of(tracks));
    }

    static Layout layout(String name, boolean isDefault) {
        return new Layout(LayoutId.of(UUID.randomUUID()), name, isDefault);
    }
}
