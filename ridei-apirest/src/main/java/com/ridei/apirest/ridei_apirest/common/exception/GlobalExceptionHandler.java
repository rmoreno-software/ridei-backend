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
 * GlobalExceptionHandler
 *
 * <p>
 *     A centralized exception handler for REST controllers. Annotated with {@link RestControllerAdvice}
 *     so Spring will apply the handlers in this class to exceptions thrown by controller methods
 *     (controllers annotated with {@code @RestController} or controllers that produce {@code @ResponseBody}).
 * </p>
 *
 * <p>
 *     Responsibilities:
 *     <ul>
 *         <li>Catch and transform validation errors (MethodArgumentNotValidException) into a stable JSON shape</li>
 *         <li>Catch unexpected exceptions and return a generic 500 response in the same JSON shape</li>
 *         <li>Keep controller code simple by centralizing error formating and HTTP status mapping</li>
 *     </ul>
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private LocaleResolver localeResolver;

    /**
     * Handle validation failures raised by the {@code @valid} annotation on controller method parameters.
     *
     * <p>
     *     Spring throws {@link MethodArgumentNotValidException} when validation on an object annotated with
     *     {@code @Valid} fails (for example, a request body DTO). This handler:
     * </p>
     * <ol>
     *     <li>Extracts {@link FieldError}s from the exception's BindingResult</li>
     *     <li>Created a simple map where keys are filed names and values are the validation messages</li>
     *     <li>Logs the full stack trace with WARN level</li>
     *     <li>Builds an {@link ApiResponseDto}</li> with a standard "Validation failed" message and the map of errors</li>
     *     <li>Returns the response with HTTP 400 BAD REQUEST</li>
     * </ol>
     *
     * <p>
     *     Notes & behavior:
     *     <ul>
     *          <li>The current implementation uses {@link FieldError#getDefaultMessage()} which is typically the
     *              message from the constraint. If you use message interpolation & i18n, the message will reflect
     *              the configured message source and locale resolution.</li>
     *     </ul>
     * </p>
     *
     * @param ex the validation exception thrown by Spring when @Valid annotated argument fails validation
     * @return a {@link ResponseEntity} with {@link ApiResponseDto} body and HTTP status 400
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
     * Handles any unhandled exceptions that are not explicitly caught elsewhere.
     *
     * <p>
     *     Logs the full stack trace with ERROR level.
     *     Provides a generic "Internal Server Error" response to the client.
     *     This ensures that internal stack traces are not leaked and the JSON response format stays consistent.
     * </p>
     *
     * @param ex unhandled exception
     * @return generic API error response with HTTP 500
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
