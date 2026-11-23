package com.ridei.sessions.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.ridei.sessions.application.port.in.PreviewManualTimesImportUseCase;
import com.ridei.sessions.domain.model.CatalogVenue;
import com.ridei.sessions.domain.port.out.VenueCatalogPort;
import com.ridei.sessions.domain.service.CatalogMatcher;
import com.ridei.sessions.domain.service.ManualTimeNotesParser;
import com.ridei.sessions.domain.service.ManualTimeNotesParser.ParsedLine;

/**
 * Interpreta las notas pegadas (22C) y busca cada circuito en el catálogo. No guarda nada: la app
 * enseña lo detectado, el piloto revisa lo dudoso y luego guarda con la importación por lotes.
 */
public class PreviewManualTimesImportService implements PreviewManualTimesImportUseCase {

    /** Cada circuito distinto son hasta dos búsquedas en circuits. */
    public static final int MAX_DISTINCT_CIRCUITS = 30;

    private final VenueCatalogPort catalog;
    private final Clock clock;

    public PreviewManualTimesImportService(VenueCatalogPort catalog, Clock clock) {
        this.catalog = catalog;
        this.clock = clock;
    }

    @Override
    public ManualTimesImportPreview preview(String notes) {
        List<ParsedLine> parsed = ManualTimeNotesParser.parse(notes, LocalDate.now(clock));
        long distinct = parsed.stream().map(ParsedLine::circuitText).filter(t -> t != null).map(PreviewManualTimesImportService::key).distinct().count();
        if (distinct > MAX_DISTINCT_CIRCUITS) {
            throw new IllegalArgumentException("At most " + MAX_DISTINCT_CIRCUITS + " different circuits per import");
        }

        Map<String, Optional<CatalogVenue>> venues = new HashMap<>();
        List<ManualTimesImportPreview.Line> lines = new ArrayList<>();
        for (ParsedLine line : parsed) {
            CatalogMatcher.Match match = null;
            if (line.circuitText() != null) {
                Optional<CatalogVenue> venue = venues.computeIfAbsent(key(line.circuitText()), k -> find(line.circuitText()));
                match = venue.map(v -> CatalogMatcher.match(v, line.circuitText())).orElse(null);
            }
            lines.add(new ManualTimesImportPreview.Line(line, match));
        }
        return new ManualTimesImportPreview(lines);
    }

    /**
     * Primero el texto entero; si no hay nada, sin la última palabra, que suele ser el trazado o la
     * pista ("Montmeló nacional").
     */
    private Optional<CatalogVenue> find(String circuitText) {
        List<String> words = Arrays.asList(circuitText.strip().split("\\s+"));
        for (int n = words.size(); n >= Math.max(1, words.size() - 1); n--) {
            List<CatalogVenue> hits = catalog.search(String.join(" ", words.subList(0, n)), 1);
            if (!hits.isEmpty()) return Optional.of(hits.get(0));
        }
        return Optional.empty();
    }

    private static String key(String circuitText) {
        return circuitText.strip().toLowerCase(Locale.ROOT);
    }
}
