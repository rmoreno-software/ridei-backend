package com.ridei.apirest.ridei_apirest.common.exception;

import com.ridei.apirest.ridei_apirest.common.response.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.LocaleResolver;

import java.util.*;

/**
 * Global exception handler for the Ridei REST API.
 *
 * <p>This class centralizes the handling of exceptions thrown by REST controllers,
 * ensuring that all errors are returned in a consistent and structured format
 * using {@link ApiResponseDto}.</p>
 *
 * <p>It provides internationalized (i18n) error messages through {@link MessageSource}
 * and supports locale resolution via {@link LocaleResolver}. Each response includes a
 * correlation ID (via {@link MDC}) for easier tracking across logs and distributed systems.</p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *     <li>Handle validation errors thrown by {@link MethodArgumentNotValidException}.</li>
 *     <li>Provide user-friendly and localized validation error messages.</li>
 *     <li>Handle all other uncaught exceptions with a generic error response.</li>
 *     <li>Log relevant error information for debugging and monitoring.</li>
 * </ul>
 *
 * <p><b>Example responses:</b></p>
 * <pre>
 * {
 *   "success": false,
 *   "message": "Validation failed",
 *   "errors": {
 *     "email": ["Email is required", "Email format not valid"],
 *     "password": ["Password must have at least 6 characters"]
 *   },
 *   "correlationId": "e7b8c123-4567-89ab-cdef-0123456789ab"
 * }
 * </pre>
 *
 * <p>For unexpected errors, a more generic response is returned:</p>
 * <pre>
 * {
 *   "success": false,
 *   "message": "An internal server error occurred. Please try again later.",
 *   "correlationId": "e7b8c123-4567-89ab-cdef-0123456789ab"
 * }
 * </pre>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private LocaleResolver localeResolver;

    /**
     * Handles validation errors thrown when method arguments fail validation.
     *
     * <p>This method captures all {@link FieldError} instances from the binding result
     * of a {@link MethodArgumentNotValidException}. Each field may contain multiple
     * validation messages (e.g., both {@code @NotBlank} and {@code @Email} can fail).
     * The messages are localized using the configured message source.</p>
     *
     * @param ex the validation exception containing invalid field details
     * @param request the current HTTP request, used to determine locale
     * @return a {@link ResponseEntity} containing a structured {@link ApiResponseDto}
     *         with field-specific validation messages and a correlation ID
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        // Using List<String> per field to capture multiple violations (e.g., @NotBlank + @Email)
        Map<String, List<String>> errors = new HashMap<>();

        // Iterate field errors and populate the map
        // Each FieldError represents a single validation failure on a specific field.
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField();
            String message = fieldError.getDefaultMessage();

            // Initialize list if not yet present
            errors.computeIfAbsent(fieldName, key -> new ArrayList<>()).add(message);
        }

        // Log warning (client-side issue)
        log.warn("Validation failed for {} fields(s): {}", errors.size(), errors);

        String correlationId = MDC.get("correlationId");

        // Build a structured error response with multiple messages per field
        String message = messageSource.getMessage("validation.failed", null, localeResolver.resolveLocale(request));
        ApiResponseDto<Void> response = ApiResponseDto.error(message, errors, correlationId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles any unexpected exceptions that occur during request processing.
     *
     * <p>This method acts as a catch-all fallback for unhandled exceptions,
     * preventing the API from exposing sensitive internal details. The error message
     * is localized based on the request locale, and detailed logs are produced for developers.</p>
     *
     * @param ex the unexpected exception
     * @param request the current HTTP request, used to determine locale
     * @return a {@link ResponseEntity} containing a generic error {@link ApiResponseDto}
     *         with a correlation ID for tracking
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {
        // Log detailed exception stack trace for debugging
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        String correlationId = MDC.get("correlationId");
        // Create a generic error response to avoid exposing internal details
        String message = messageSource.getMessage("internal.server.error", null, localeResolver.resolveLocale(request));
        ApiResponseDto<Void> response = ApiResponseDto.error(message, correlationId);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
