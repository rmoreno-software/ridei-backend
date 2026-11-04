package com.ridei.circuits.infrastructure.adapter.in.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.ridei.circuits.domain.exception.CatalogItemAlreadyExistsException;
import com.ridei.circuits.domain.exception.ConcurrentCatalogModificationException;
import com.ridei.circuits.domain.exception.DuplicateLayoutException;
import com.ridei.circuits.domain.exception.DuplicateTrackException;
import com.ridei.circuits.domain.exception.InvalidMergeException;
import com.ridei.circuits.domain.exception.LayoutNotFoundException;
import com.ridei.circuits.domain.exception.MergedCatalogItemException;
import com.ridei.circuits.domain.exception.PossibleDuplicateVenueException;
import com.ridei.circuits.domain.exception.TrackNotFoundException;
import com.ridei.circuits.domain.exception.VenueNotFoundException;
import com.ridei.circuits.infrastructure.adapter.in.rest.exception.ApiError;
import com.ridei.circuits.infrastructure.adapter.in.rest.exception.CandidateDTO;
import com.ridei.circuits.infrastructure.adapter.in.rest.exception.ConflictApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 400 ─────────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getAllErrors().stream()
            .map(GlobalExceptionHandler::describe)
            .toList();
        return ResponseEntity.badRequest().body(
            new ApiError(400, "Validation failed", "One or more fields are invalid", details));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
        String message = ex instanceof MethodArgumentTypeMismatchException mismatch
            ? "Invalid value for '" + mismatch.getName() + "'"
            : ex.getMessage();
        return ResponseEntity.badRequest().body(new ApiError(400, "Bad Request", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiError(400, "Bad Request", "Malformed request body"));
    }

    // ── 404 ─────────────────────────────────────────────────────────────────────────────

    @ExceptionHandler({VenueNotFoundException.class, TrackNotFoundException.class, LayoutNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(404, "Not Found", ex.getMessage()));
    }

    // ── 409 ─────────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(PossibleDuplicateVenueException.class)
    public ResponseEntity<ApiError> handlePossibleDuplicateVenue(PossibleDuplicateVenueException ex) {
        List<CandidateDTO> candidates = ex.getCandidates().stream().map(CandidateDTO::fromVenue).toList();
        return conflict(new ConflictApiError(ConflictApiError.POSSIBLE_DUPLICATE_VENUE, ex.getMessage(), candidates));
    }

    @ExceptionHandler(DuplicateTrackException.class)
    public ResponseEntity<ApiError> handleDuplicateTrack(DuplicateTrackException ex) {
        CandidateDTO existing = CandidateDTO.fromTrack(ex.getExisting(), ex.getSimilarity());
        return conflict(new ConflictApiError(ConflictApiError.DUPLICATE_TRACK, ex.getMessage(), List.of(existing)));
    }

    @ExceptionHandler(DuplicateLayoutException.class)
    public ResponseEntity<ApiError> handleDuplicateLayout(DuplicateLayoutException ex) {
        CandidateDTO existing = CandidateDTO.fromLayout(ex.getExisting(), ex.getSimilarity());
        return conflict(new ConflictApiError(ConflictApiError.DUPLICATE_LAYOUT, ex.getMessage(), List.of(existing)));
    }

    @ExceptionHandler(MergedCatalogItemException.class)
    public ResponseEntity<ApiError> handleMerged(MergedCatalogItemException ex) {
        return conflict(new ConflictApiError(ConflictApiError.CATALOG_ITEM_MERGED, ex.getMessage()));
    }

    /** Última barrera: una restricción UNIQUE rechazó la escritura incluso tras reintentar. */
    @ExceptionHandler({CatalogItemAlreadyExistsException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> handleAlreadyExists(RuntimeException ex) {
        log.info("Catalog write rejected by a database constraint: {}", ex.getMessage());
        return conflict(new ConflictApiError(ConflictApiError.CATALOG_ITEM_ALREADY_EXISTS,
            "The catalog item conflicts with an existing one; reload and try again"));
    }

    @ExceptionHandler({ConcurrentCatalogModificationException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> handleConcurrentModification(RuntimeException ex) {
        return conflict(new ConflictApiError(ConflictApiError.CONCURRENT_MODIFICATION,
            "The venue was modified concurrently; reload and try again"));
    }

    // ── 422 / 405 / 500 ─────────────────────────────────────────────────────────────────

    @ExceptionHandler(InvalidMergeException.class)
    public ResponseEntity<ApiError> handleInvalidMerge(InvalidMergeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ApiError(422, "Unprocessable Entity", ex.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ApiError(405, "Method Not Allowed", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError(500, "Internal Server Error", "An unexpected error occurred"));
    }

    private static ResponseEntity<ApiError> conflict(ConflictApiError body) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private static String describe(ObjectError error) {
        return error instanceof FieldError field && "typeMismatch".equals(field.getCode())
            ? "Invalid value for '" + field.getField() + "'"
            : error.getDefaultMessage();
    }
}
