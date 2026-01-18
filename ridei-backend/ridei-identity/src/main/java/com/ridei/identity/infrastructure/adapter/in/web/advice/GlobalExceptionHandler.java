package com.ridei.identity.infrastructure.adapter.in.web.advice;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ridei.identity.domain.exception.UserAlreadyExistsException;
import com.ridei.identity.infrastructure.adapter.in.web.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized Exception Handler (Advice) for the Web Adapter.
 * <p>
 * This class acts as a global safety net that intercepts exceptions thrown anywhere
 * in the request processing pipeline (Controller, Service, Domain).
 * It translates internal Java exceptions into standardized HTTP responses (RFC 7807 or custom DTOs),
 * ensuring that the API clients always receive consistent error structures.
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation failures for DTOs annotated with {@code @Valid}.
     * <p>
     * Triggered automatically by Spring when the JSON body does not match the
     * constraints defined in the {@code RegisterRequest} (e.g., invalid email format).
     * </p>
     *
     * @param ex The exception containing the list of field violations.
     * @return HTTP 400 (Bad Request) with a concatenated string of validation error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.debug("Validation failed: {}", errorMessage);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(400, "Validation error: " + errorMessage));
    }

    /**
     * Handles specific Business Domain exceptions.
     * <p>
     * Maps the {@link UserAlreadyExistsException} (Domain Layer) to the appropriate
     * HTTP 409 (Conflict) status, indicating that the resource cannot be created
     * due to current state conflicts.
     * </p>
     *
     * @param ex The domain exception.
     * @return HTTP 409 (Conflict) with the business error message.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {

        log.warn("Business exception: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(409, ex.getMessage()));
    }

    /**
     * Catch-all handler for unspecified RuntimeExceptions.
     * <p>
     * <b>Note:</b> This maps generic runtime errors to HTTP 400. Ensure that
     * this behavior is desired, as some RuntimeExceptions (like NullPointerException)
     * might actually represent 500 Server Errors.
     * </p>
     *
     * @param ex The generic runtime exception.
     * @return HTTP 400 (Bad Request).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {

        log.error("Runtime exception caught: ", ex);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(400, ex.getMessage()));
    }

    /**
     * Final safety net for unexpected system failures.
     * <p>
     * This handler prevents raw stack traces from leaking to the client,
     * replacing them with a generic "Internal Server Error" message.
     * </p>
     *
     * @param ex The unexpected exception.
     * @return HTTP 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {

        log.error("Unexpected system error: ", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(500, "Internal Server Error"));
    }    
}
