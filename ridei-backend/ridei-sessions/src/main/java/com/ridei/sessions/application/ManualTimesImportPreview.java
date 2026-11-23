package com.ridei.sessions.application;

import java.util.List;
import java.util.Optional;

import com.ridei.sessions.domain.service.CatalogMatcher;
import com.ridei.sessions.domain.service.ManualTimeNotesParser.ParsedLine;

/** Lo que se entendió de las notas pegadas (pantalla 22C), sin guardar nada. */
public record ManualTimesImportPreview(List<Line> lines) {

    public ManualTimesImportPreview {
        lines = List.copyOf(lines);
    }

    public enum Status {
        /** Circuito, trazado, fecha y tiempo claros: se puede guardar tal cual. */
        READY,
        /** Falta algo o hay que elegir (pista, trazado, circuito no encontrado, fecha…). */
        NEEDS_REVIEW
    }

    /**
     * @param match {@code null} si no había texto de circuito o el catálogo no encontró nada
     */
    public record Line(ParsedLine parsed, CatalogMatcher.Match match) {

        public Optional<CatalogMatcher.Match> matchIfAny() {
            return Optional.ofNullable(match);
        }

        public boolean circuitNotFound() {
            return parsed.circuitText() != null && match == null;
        }

        public Status status() {
            return parsed.isComplete() && match != null && match.isResolved() ? Status.READY : Status.NEEDS_REVIEW;
        }
    }

    public long readyCount() {
        return lines.stream().filter(l -> l.status() == Status.READY).count();
    }
}
