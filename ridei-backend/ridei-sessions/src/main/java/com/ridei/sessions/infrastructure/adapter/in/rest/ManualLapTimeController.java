package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.sessions.application.ListManualLapTimesQuery;
import com.ridei.sessions.application.RecordManualLapTimeCommand;
import com.ridei.sessions.application.RecordManualLapTimeResult;
import com.ridei.sessions.application.port.in.DeleteManualLapTimeUseCase;
import com.ridei.sessions.application.port.in.GetManualLapTimeUseCase;
import com.ridei.sessions.application.port.in.ImportManualLapTimesUseCase;
import com.ridei.sessions.application.port.in.ListManualLapTimesUseCase;
import com.ridei.sessions.application.port.in.PreviewManualTimesImportUseCase;
import com.ridei.sessions.application.port.in.RecordManualLapTimeUseCase;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

/**
 * Tiempos apuntados a mano de tandas pasadas (pantallas 22–22E). El piloto sale siempre del token.
 * Va bajo {@code /api/v1/sessions} para compartir la regla del túnel; las rutas literales ganan a
 * {@code /api/v1/sessions/{sessionId}}.
 */
@RestController
@RequestMapping("/api/v1/sessions/manual-times")
public class ManualLapTimeController {

    private final RecordManualLapTimeUseCase recordManualLapTime;
    private final ImportManualLapTimesUseCase importManualLapTimes;
    private final PreviewManualTimesImportUseCase previewImport;
    private final ListManualLapTimesUseCase listManualLapTimes;
    private final GetManualLapTimeUseCase getManualLapTime;
    private final DeleteManualLapTimeUseCase deleteManualLapTime;

    public ManualLapTimeController(
        RecordManualLapTimeUseCase recordManualLapTime,
        ImportManualLapTimesUseCase importManualLapTimes,
        PreviewManualTimesImportUseCase previewImport,
        ListManualLapTimesUseCase listManualLapTimes,
        GetManualLapTimeUseCase getManualLapTime,
        DeleteManualLapTimeUseCase deleteManualLapTime
    ) {
        this.recordManualLapTime = recordManualLapTime;
        this.importManualLapTimes = importManualLapTimes;
        this.previewImport = previewImport;
        this.listManualLapTimes = listManualLapTimes;
        this.getManualLapTime = getManualLapTime;
        this.deleteManualLapTime = deleteManualLapTime;
    }

    @Operation(summary = "Record a manual lap time",
        description = "A time from a past session, without GPS (screen 22B). Idempotent: the app generates the id, so retrying never duplicates it. 201 if new, 200 if it already existed (the body is ignored). The layout must exist in the circuits catalog (it doesn't need a finish line) and the motorbike must be in the rider's garage, current or former.")
    @PutMapping("/{id}")
    public ResponseEntity<ManualLapTimeResponseDTO> record(
        @PathVariable String id,
        @RequestBody @Valid ManualLapTimeRequestDTO dto,
        Authentication authentication
    ) {
        ManualLapTimeId timeId = ManualLapTimeId.of(id);
        if (dto.id() != null && !ManualLapTimeId.of(dto.id()).equals(timeId)) {
            throw new IllegalArgumentException("The id in the body does not match the path");
        }
        RecordManualLapTimeCommand command = dto.toCommand(timeId, rider(authentication));
        RecordManualLapTimeResult result;
        try {
            result = recordManualLapTime.record(command);
        } catch (DataIntegrityViolationException e) {
            // Dos guardados simultáneos con el mismo id: el segundo es un reintento.
            result = recordManualLapTime.record(command);
        }
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
            .body(ManualLapTimeResponseDTO.fromDomain(result.time()));
    }

    @Operation(summary = "Save imported manual lap times",
        description = "Saves the reviewed lines of an import (screen 22C) at once: all or none. Each time carries its own id (idempotent). 201 if any was new, 200 if all already existed.")
    @PostMapping("/batch")
    public ResponseEntity<List<ManualLapTimeResponseDTO>> recordAll(
        @RequestBody @Valid ImportManualLapTimesRequestDTO dto,
        Authentication authentication
    ) {
        RiderId rider = rider(authentication);
        List<RecordManualLapTimeCommand> commands = dto.times().stream().map(t -> t.toCommand(rider)).toList();
        List<RecordManualLapTimeResult> results;
        try {
            results = importManualLapTimes.recordAll(commands);
        } catch (DataIntegrityViolationException e) {
            results = importManualLapTimes.recordAll(commands);
        }
        boolean created = results.stream().anyMatch(RecordManualLapTimeResult::created);
        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK)
            .body(results.stream().map(r -> ManualLapTimeResponseDTO.fromDomain(r.time())).toList());
    }

    @Operation(summary = "Preview an import from notes",
        description = "Parses pasted notes, one session per line with circuit, date and time in any order (e.g. 'Castellolí 2/6/24 1:14,22 (lluvia)'), and looks each circuit up in the catalog. Saves nothing. Lines whose track or layout can't be decided come back as NEEDS_REVIEW with the venue's tracks and layouts to choose from.")
    @PostMapping("/import/preview")
    public ManualTimesImportPreviewResponseDTO preview(@RequestBody @Valid PreviewManualTimesImportRequestDTO dto) {
        return ManualTimesImportPreviewResponseDTO.fromDomain(previewImport.preview(dto.notes()));
    }

    @Operation(summary = "My manual lap times", description = "Most recent date first. Optional filters: layoutId, motorbikeId.")
    @GetMapping
    public List<ManualLapTimeResponseDTO> list(
        @RequestParam(required = false) String layoutId,
        @RequestParam(required = false) String motorbikeId,
        Authentication authentication
    ) {
        ListManualLapTimesQuery query = new ListManualLapTimesQuery(
            rider(authentication),
            layoutId != null ? LayoutId.of(layoutId) : null,
            motorbikeId != null ? MotorbikeId.of(motorbikeId) : null);
        return listManualLapTimes.list(query).stream().map(ManualLapTimeResponseDTO::fromDomain).toList();
    }

    @GetMapping("/{id}")
    public ManualLapTimeResponseDTO get(@PathVariable String id, Authentication authentication) {
        return ManualLapTimeResponseDTO.fromDomain(getManualLapTime.get(rider(authentication), ManualLapTimeId.of(id)));
    }

    @Operation(summary = "Delete a manual lap time", description = "Manual times can't be edited: delete and record again.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication authentication) {
        deleteManualLapTime.delete(rider(authentication), ManualLapTimeId.of(id));
        return ResponseEntity.noContent().build();
    }

    private static RiderId rider(Authentication authentication) {
        return RiderId.of(authentication.getName());
    }
}
