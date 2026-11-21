package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.sessions.application.LapReference;
import com.ridei.sessions.application.ListSessionsQuery;
import com.ridei.sessions.application.SessionSummary;
import com.ridei.sessions.application.port.in.DeleteSessionUseCase;
import com.ridei.sessions.application.port.in.EditSessionUseCase;
import com.ridei.sessions.application.port.in.GetCornerAnalysisUseCase;
import com.ridei.sessions.application.port.in.GetLapTraceUseCase;
import com.ridei.sessions.application.port.in.GetPersonalRecordsUseCase;
import com.ridei.sessions.application.port.in.GetReferenceLapUseCase;
import com.ridei.sessions.application.port.in.GetSessionUseCase;
import com.ridei.sessions.application.port.in.ListSessionsUseCase;
import com.ridei.sessions.application.port.in.RecalculateSessionUseCase;
import com.ridei.sessions.application.port.in.RecordSessionUseCase;
import com.ridei.sessions.domain.exception.NoValidLapsException;
import com.ridei.sessions.domain.exception.SessionIdConflictException;
import com.ridei.sessions.domain.exception.SessionNotFoundException;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

/** Sesiones del piloto autenticado: el piloto sale siempre del token, nunca del cuerpo ni la ruta. */
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final RecordSessionUseCase recordSession;
    private final ListSessionsUseCase listSessions;
    private final GetSessionUseCase getSession;
    private final EditSessionUseCase editSession;
    private final RecalculateSessionUseCase recalculateSession;
    private final DeleteSessionUseCase deleteSession;
    private final GetLapTraceUseCase lapTraces;
    private final GetCornerAnalysisUseCase cornerAnalysis;
    private final GetPersonalRecordsUseCase personalRecords;
    private final GetReferenceLapUseCase referenceLap;

    public SessionController(
        RecordSessionUseCase recordSession,
        ListSessionsUseCase listSessions,
        GetSessionUseCase getSession,
        EditSessionUseCase editSession,
        RecalculateSessionUseCase recalculateSession,
        DeleteSessionUseCase deleteSession,
        GetLapTraceUseCase lapTraces,
        GetCornerAnalysisUseCase cornerAnalysis,
        GetPersonalRecordsUseCase personalRecords,
        GetReferenceLapUseCase referenceLap
    ) {
        this.recordSession = recordSession;
        this.listSessions = listSessions;
        this.getSession = getSession;
        this.editSession = editSession;
        this.recalculateSession = recalculateSession;
        this.deleteSession = deleteSession;
        this.lapTraces = lapTraces;
        this.cornerAnalysis = cornerAnalysis;
        this.personalRecords = personalRecords;
        this.referenceLap = referenceLap;
    }

    @Operation(summary = "Upload a recorded session",
        description = "Idempotent: the app generates the session id when it starts recording, so retrying an upload never duplicates it. 201 on first upload, 200 if it already existed (the body is ignored). Laps and sectors are timed on upload against the layout's finish line from the circuits catalog.")
    @PutMapping("/{sessionId}")
    public ResponseEntity<SessionDetailResponseDTO> upload(
        @PathVariable String sessionId,
        @RequestBody @Valid RecordSessionRequestDTO dto,
        Authentication authentication
    ) {
        SessionId id = SessionId.of(sessionId);
        RiderId rider = rider(authentication);
        boolean created;
        try {
            created = recordSession.record(dto.toCommand(id, rider)).created();
        } catch (DataIntegrityViolationException e) {
            // Dos subidas simultáneas con el mismo id (la app reintenta mientras la primera aún
            // viaja): la segunda choca con la clave primaria al confirmar. Es un reintento más.
            created = false;
        }
        SessionSummary summary;
        try {
            summary = getSession.get(rider, id);
        } catch (SessionNotFoundException e) {
            if (!created) throw new SessionIdConflictException();
            throw e;
        }
        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK)
            .body(SessionDetailResponseDTO.fromSummary(summary));
    }

    @Operation(summary = "My sessions", description = "Most recent first. Optional filters: motorbikeId, layoutId, categoryGroup (ASPHALT, MIXED, OFF_ROAD).")
    @GetMapping
    public List<SessionListItemDTO> list(
        @RequestParam(required = false) String motorbikeId,
        @RequestParam(required = false) String layoutId,
        @RequestParam(required = false) String categoryGroup,
        Authentication authentication
    ) {
        ListSessionsQuery query = new ListSessionsQuery(
            rider(authentication),
            motorbikeId != null ? MotorbikeId.of(motorbikeId) : null,
            layoutId != null ? LayoutId.of(layoutId) : null,
            categoryGroup);
        return listSessions.list(query).stream().map(SessionListItemDTO::fromSummary).toList();
    }

    @Operation(summary = "Personal records", description = "Best lap, historic ideal lap and evolution per (layout, motorbike). Optional filters: layoutId, motorbikeId.")
    @GetMapping("/records")
    public List<PersonalRecordResponseDTO> records(
        @RequestParam(required = false) String layoutId,
        @RequestParam(required = false) String motorbikeId,
        Authentication authentication
    ) {
        return personalRecords.records(
                rider(authentication),
                layoutId != null ? LayoutId.of(layoutId) : null,
                motorbikeId != null ? MotorbikeId.of(motorbikeId) : null)
            .stream().map(PersonalRecordResponseDTO::fromDomain).toList();
    }

    @Operation(summary = "Reference lap for the live HUD", description = "The rider's record lap with this motorbike on this layout, with the gates it was timed with. 204 if there is none yet.")
    @GetMapping("/reference-lap")
    public ResponseEntity<ReferenceLapResponseDTO> referenceLap(
        @RequestParam String layoutId,
        @RequestParam String motorbikeId,
        Authentication authentication
    ) {
        return referenceLap.reference(rider(authentication), LayoutId.of(layoutId), MotorbikeId.of(motorbikeId))
            .map(ReferenceLapResponseDTO::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Session summary with laps and sectors")
    @GetMapping("/{sessionId}")
    public SessionDetailResponseDTO get(@PathVariable String sessionId, Authentication authentication) {
        return SessionDetailResponseDTO.fromSummary(getSession.get(rider(authentication), SessionId.of(sessionId)));
    }

    @Operation(summary = "Edit name, conditions or notes")
    @PatchMapping("/{sessionId}")
    public SessionDetailResponseDTO edit(
        @PathVariable String sessionId,
        @RequestBody @Valid EditSessionRequestDTO dto,
        Authentication authentication
    ) {
        Session session = editSession.edit(dto.toCommand(rider(authentication), SessionId.of(sessionId)));
        return SessionDetailResponseDTO.fromSummary(getSession.get(rider(authentication), session.id()));
    }

    @Operation(summary = "Re-time the session", description = "Re-times the stored telemetry with the layout's current finish line and drawing from the circuits catalog (e.g. after correcting the finish line).")
    @PostMapping("/{sessionId}/recalculate")
    public SessionDetailResponseDTO recalculate(@PathVariable String sessionId, Authentication authentication) {
        Session session = recalculateSession.recalculate(rider(authentication), SessionId.of(sessionId));
        return SessionDetailResponseDTO.fromSummary(getSession.get(rider(authentication), session.id()));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(@PathVariable String sessionId, Authentication authentication) {
        deleteSession.delete(rider(authentication), SessionId.of(sessionId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ideal lap", description = "Best time of each sector and the lap it comes from. 422 if no valid lap has every sector timed.")
    @GetMapping("/{sessionId}/ideal-lap")
    public IdealLapResponseDTO idealLap(@PathVariable String sessionId, Authentication authentication) {
        Session session = getSession.get(rider(authentication), SessionId.of(sessionId)).session();
        return IdealLapResponseDTO.from(
            session,
            session.idealLap().orElseThrow(NoValidLapsException::new),
            session.bestLap().orElseThrow(NoValidLapsException::new));
    }

    @Operation(summary = "Ideal lap trace", description = "The ideal lap stitched from the best sectors, indexed by distance.")
    @GetMapping("/{sessionId}/ideal-lap/trace")
    public LapTraceResponseDTO idealLapTrace(@PathVariable String sessionId, Authentication authentication) {
        return LapTraceResponseDTO.fromDomain(lapTraces.idealTrace(rider(authentication), SessionId.of(sessionId)));
    }

    @Operation(summary = "Lap trace", description = "Position, speed and lean along the lap, indexed by distance from the finish line (lap viewer).")
    @GetMapping("/{sessionId}/laps/{lap}/trace")
    public LapTraceResponseDTO lapTrace(@PathVariable String sessionId, @PathVariable int lap, Authentication authentication) {
        return LapTraceResponseDTO.fromDomain(lapTraces.trace(rider(authentication), SessionId.of(sessionId), lap));
    }

    @Operation(summary = "Speed–distance comparison", description = "A lap against compareTo = ideal (default), best or another lap number, with the cumulative delta.")
    @GetMapping("/{sessionId}/laps/{lap}/comparison")
    public LapComparisonResponseDTO comparison(
        @PathVariable String sessionId,
        @PathVariable int lap,
        @RequestParam(required = false, defaultValue = "ideal") String compareTo,
        Authentication authentication
    ) {
        return LapComparisonResponseDTO.fromDomain(
            lapTraces.compare(rider(authentication), SessionId.of(sessionId), lap, LapReference.parse(compareTo)));
    }

    @Operation(summary = "Corner analysis", description = "Corners detected on the best lap, every valid lap's pass through each one, and the ranking of where time is lost.")
    @GetMapping("/{sessionId}/corners")
    public CornerAnalysisResponseDTO corners(@PathVariable String sessionId, Authentication authentication) {
        return CornerAnalysisResponseDTO.fromDomain(cornerAnalysis.analyze(rider(authentication), SessionId.of(sessionId)));
    }

    private static RiderId rider(Authentication authentication) {
        return RiderId.of(authentication.getName());
    }
}
