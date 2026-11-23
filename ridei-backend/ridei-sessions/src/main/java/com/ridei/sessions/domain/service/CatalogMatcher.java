package com.ridei.sessions.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ridei.sessions.domain.model.CatalogVenue;
import com.ridei.sessions.domain.model.LayoutRef;

/**
 * Dentro de un complejo ya encontrado, elige pista y trazado a partir de lo que escribió el piloto:
 * "Montmeló nacional" → pista Velocidad, trazado Nacional. Si no lo dice, la única pista y su
 * trazado por defecto. Si el complejo tiene varias pistas y el texto no aclara cuál, no adivina.
 */
public final class CatalogMatcher {

    /** Palabras que no distinguen una pista o un trazado de otro. */
    private static final Set<String> STOPWORDS = Set.of(
        "circuit", "circuito", "circuits", "circuitos", "track", "pista", "de", "del", "la", "el", "les", "los", "las");

    public enum Issue {
        /** Varias pistas y el texto no dice cuál (Alcarràs: velocidad, karting, motocross). */
        TRACK_AMBIGUOUS,
        /** Varios trazados, ninguno por defecto y el texto no dice cuál. */
        LAYOUT_AMBIGUOUS,
        /** La pista (o el complejo) aún no tiene trazados: no se puede apuntar ahí todavía. */
        NO_LAYOUTS
    }

    /** {@code track} y {@code layout} son {@code null} cuando hay {@code issue}. */
    public record Match(CatalogVenue venue, CatalogVenue.Track track, CatalogVenue.Layout layout, Issue issue) {
        public boolean isResolved() {
            return issue == null;
        }

        public Optional<LayoutRef> layoutRef() {
            return isResolved() ? Optional.of(venue.ref(track, layout)) : Optional.empty();
        }
    }

    private CatalogMatcher() {}

    public static Match match(CatalogVenue venue, String text) {
        List<String> hints = TextNormalizer.words(text).stream()
            .filter(w -> w.length() >= 3 && !STOPWORDS.contains(w))
            .toList();
        // La duda se mide sobre todas las pistas, tengan trazados o no: si solo una tiene trazado,
        // un tiempo de "Alcarras" a secas puede ser del karting y no hay que asignarlo a velocidad.
        List<CatalogVenue.Track> tracks = venue.tracks();
        if (tracks.isEmpty()) return new Match(venue, null, null, Issue.NO_LAYOUTS);

        List<CatalogVenue.Track> candidates = tracks.stream()
            .filter(t -> mentions(t.name(), hints) || t.layouts().stream().anyMatch(l -> mentions(l.name(), hints)))
            .toList();
        if (candidates.isEmpty()) candidates = tracks;
        if (candidates.size() > 1) return new Match(venue, null, null, Issue.TRACK_AMBIGUOUS);

        CatalogVenue.Track track = candidates.get(0);
        if (track.layouts().isEmpty()) return new Match(venue, null, null, Issue.NO_LAYOUTS);
        List<CatalogVenue.Layout> named = track.layouts().stream().filter(l -> mentions(l.name(), hints)).toList();
        Optional<CatalogVenue.Layout> layout = named.size() == 1 ? Optional.of(named.get(0)) : track.defaultLayout();
        return layout
            .map(l -> new Match(venue, track, l, null))
            .orElseGet(() -> new Match(venue, track, null, Issue.LAYOUT_AMBIGUOUS));
    }

    /** Alguna palabra del texto es una palabra del nombre (sin tildes ni mayúsculas). */
    private static boolean mentions(String name, List<String> hints) {
        if (name == null || hints.isEmpty()) return false;
        List<String> nameWords = TextNormalizer.words(name);
        return hints.stream().anyMatch(nameWords::contains);
    }
}
