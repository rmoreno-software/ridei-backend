package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ridei.sessions.application.ManualTimesImportPreview;
import com.ridei.sessions.domain.model.CatalogVenue;
import com.ridei.sessions.domain.model.LapTimePrecision;
import com.ridei.sessions.domain.model.TrackCondition;
import com.ridei.sessions.domain.service.CatalogMatcher;
import com.ridei.sessions.domain.service.ManualTimeNotesParser.ParsedLine;
import com.ridei.sessions.domain.service.ManualTimeNotesParser.Problem;

/**
 * "Detectados · 4 — 3 listos · 1 por revisar" (22C). Nada se ha guardado: la app enseña las líneas,
 * deja elegir pista o trazado en las dudosas y guarda con {@code POST /manual-times/batch}.
 *
 * @param lines en el orden de las notas
 */
public record ManualTimesImportPreviewResponseDTO(int detectedCount, int readyCount, int needsReviewCount, List<LineDTO> lines) {

    /**
     * @param status   READY o NEEDS_REVIEW
     * @param problems MISSING_CIRCUIT, CIRCUIT_NOT_FOUND, TRACK_AMBIGUOUS, LAYOUT_AMBIGUOUS,
     *                 NO_LAYOUTS, MISSING_DATE, INVALID_DATE, MISSING_TIME, TIME_OUT_OF_RANGE
     * @param venue    complejo encontrado; {@code null} si ninguno
     * @param layout   trazado elegido; {@code null} si hay que elegirlo
     * @param tracks   pistas y trazados del complejo cuando hay que elegir ("Elegir pista"); vacío si no
     * @param notes    texto entre paréntesis que no era una condición
     */
    public record LineDTO(
        int lineNumber,
        String text,
        ManualTimesImportPreview.Status status,
        List<String> problems,
        String circuitText,
        ManualLapTimeResponseDTO.RefDTO venue,
        String trackName,
        ManualLapTimeResponseDTO.RefDTO layout,
        LocalDate date,
        Long bestLapMillis,
        LapTimePrecision precision,
        List<Long> otherLapsMillis,
        TrackCondition trackCondition,
        String notes,
        List<TrackOptionDTO> tracks
    ) {}

    public record TrackOptionDTO(String name, String discipline, List<LayoutOptionDTO> layouts) {}

    public record LayoutOptionDTO(UUID id, String name, @JsonProperty("isDefault") boolean isDefault) {}

    public static ManualTimesImportPreviewResponseDTO fromDomain(ManualTimesImportPreview preview) {
        List<LineDTO> lines = preview.lines().stream().map(ManualTimesImportPreviewResponseDTO::line).toList();
        int ready = (int) preview.readyCount();
        return new ManualTimesImportPreviewResponseDTO(lines.size(), ready, lines.size() - ready, lines);
    }

    private static LineDTO line(ManualTimesImportPreview.Line line) {
        ParsedLine p = line.parsed();
        CatalogMatcher.Match match = line.match();
        List<String> problems = new ArrayList<>();
        for (Problem problem : p.problems()) {
            if (problem == Problem.MISSING_CIRCUIT) problems.add(problem.name());
        }
        if (line.circuitNotFound()) problems.add("CIRCUIT_NOT_FOUND");
        if (match != null && !match.isResolved()) problems.add(match.issue().name());
        for (Problem problem : p.problems()) {
            if (problem != Problem.MISSING_CIRCUIT) problems.add(problem.name());
        }

        boolean resolved = match != null && match.isResolved();
        return new LineDTO(
            p.lineNumber(), p.text(), line.status(), problems, p.circuitText(),
            match != null ? new ManualLapTimeResponseDTO.RefDTO(match.venue().id().value(), match.venue().name()) : null,
            resolved ? match.track().name() : null,
            resolved ? new ManualLapTimeResponseDTO.RefDTO(match.layout().id().value(), match.layout().name()) : null,
            p.date(), p.bestLapMillis(), p.precision(), p.otherLapsMillis(), p.trackCondition(), p.note(),
            match != null && !resolved ? options(match.venue()) : List.of());
    }

    private static List<TrackOptionDTO> options(CatalogVenue venue) {
        return venue.tracks().stream()
            .filter(t -> !t.layouts().isEmpty())
            .map(t -> new TrackOptionDTO(t.name(), t.discipline(), t.layouts().stream()
                .map(l -> new LayoutOptionDTO(l.id().value(), l.name(), l.isDefault()))
                .toList()))
            .toList();
    }
}
