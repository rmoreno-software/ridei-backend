package com.ridei.circuits.application;

import static com.ridei.circuits.domain.model.CatalogFixtures.MONTMELO;
import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.montmelo;
import static com.ridei.circuits.domain.model.CatalogFixtures.track;
import static com.ridei.circuits.domain.model.CatalogFixtures.venue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.ridei.circuits.domain.exception.VenueNotFoundException;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;

class GetVenueDetailServiceTest {

    private final InMemoryVenueRepository repository = new InMemoryVenueRepository();
    private final GetVenueDetailService service = new GetVenueDetailService(repository);

    @Test
    void activeVenueIsReturnedAsIs() {
        Venue venue = repository.given(montmelo());

        VenueDetail detail = service.get(venue.getId());

        assertThat(detail.venue()).isSameAs(venue);
        assertThat(detail.redirectedFromMerged()).isFalse();
    }

    @Test
    void mergedIdFollowsTheChainToTheCanonicalVenue() {
        Venue first = repository.given(venue("Montmeló A", MONTMELO, track(Discipline.KARTING, "Karting")));
        Venue second = repository.given(venue("Montmeló B", MONTMELO, track(Discipline.MOTOCROSS, "MX")));
        Venue canonical = repository.given(montmelo());
        first.mergeInto(second, NOW);
        second.mergeInto(canonical, NOW);

        VenueDetail detail = service.get(first.getId());

        assertThat(detail.venue()).isSameAs(canonical);
        assertThat(detail.requestedId()).isEqualTo(first.getId());
        assertThat(detail.redirectedFromMerged()).isTrue();
    }

    @Test
    void unknownIdIsNotFound() {
        assertThatThrownBy(() -> service.get(VenueId.newId())).isInstanceOf(VenueNotFoundException.class);
    }
}
