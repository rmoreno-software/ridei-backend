package com.ridei.sessions.domain.model;

import java.util.List;
import java.util.Optional;

/** Complejo del catálogo con sus pistas y trazados activos, sin geometría. */
public record CatalogVenue(VenueId id, String name, List<Track> tracks) {

    public CatalogVenue {
        if (id == null) throw new IllegalArgumentException("Venue id is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Venue name is required");
        tracks = tracks == null ? List.of() : List.copyOf(tracks);
    }

    /** @param discipline la del catálogo (ROAD_RACING, KARTING…), tal cual */
    public record Track(String name, String discipline, List<Layout> layouts) {
        public Track {
            layouts = layouts == null ? List.of() : List.copyOf(layouts);
        }

        public Optional<Layout> defaultLayout() {
            return layouts.stream().filter(Layout::isDefault).findFirst()
                .or(() -> layouts.size() == 1 ? Optional.of(layouts.get(0)) : Optional.empty());
        }
    }

    public record Layout(LayoutId id, String name, boolean isDefault) {
        public Layout {
            if (id == null) throw new IllegalArgumentException("Layout id is required");
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Layout name is required");
        }
    }

    public Optional<LayoutRef> layout(LayoutId layoutId) {
        for (Track track : tracks) {
            for (Layout layout : track.layouts()) {
                if (layout.id().equals(layoutId)) return Optional.of(ref(track, layout));
            }
        }
        return Optional.empty();
    }

    public LayoutRef ref(Track track, Layout layout) {
        return new LayoutRef(id, name, track.name(), layout.id(), layout.name());
    }
}
