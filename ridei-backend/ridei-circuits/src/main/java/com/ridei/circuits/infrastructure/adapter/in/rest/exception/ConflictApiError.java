package com.ridei.circuits.infrastructure.adapter.in.rest.exception;

import java.util.List;

import lombok.Getter;

/**
 * 409 con el formato de {@link ApiError} más un {@code code} legible por máquina y, para los
 * duplicados, la lista de {@code candidates} ("¿Es alguno de estos?").
 */
@Getter
public class ConflictApiError extends ApiError {

    public static final String POSSIBLE_DUPLICATE_VENUE = "POSSIBLE_DUPLICATE_VENUE";
    public static final String DUPLICATE_TRACK = "DUPLICATE_TRACK";
    public static final String DUPLICATE_LAYOUT = "DUPLICATE_LAYOUT";
    public static final String CATALOG_ITEM_MERGED = "CATALOG_ITEM_MERGED";
    public static final String CATALOG_ITEM_ALREADY_EXISTS = "CATALOG_ITEM_ALREADY_EXISTS";
    public static final String CONCURRENT_MODIFICATION = "CONCURRENT_MODIFICATION";

    private final String code;
    private final List<CandidateDTO> candidates;

    public ConflictApiError(String code, String message, List<CandidateDTO> candidates) {
        super(409, "Conflict", message);
        this.code = code;
        this.candidates = candidates;
    }

    public ConflictApiError(String code, String message) {
        this(code, message, List.of());
    }
}
