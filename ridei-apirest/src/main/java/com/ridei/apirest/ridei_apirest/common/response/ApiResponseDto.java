package com.ridei.apirest.ridei_apirest.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ============================================
 * ✅ ApiResponseDto<T>
 * ============================================
 * <p>
 *     Generic Data Transfer Object (DTO) used as standard wrapper
 *     for all API responses in the application.
 * </p>
 *
 * <p>
 *     This class ensures a consistent structure for responses returned
 *     by controllers, whether the request was successful or not.
 * </p>
 *
 * <p>
 *     Typical JSON structure:
 *     {
 *         "success": true,
 *         "data": {...},
 *         "message": "Operation completed successfully",
 *         "errors": null,
 *         "timeStamp": "2025-10-16T14:23:45.234",
 *         "correlationId": "0a5b1d2f-0d94-4dcb-b4a8-cf377935d3a3"
 *     }
 * </p>
 *
 * @param <T> The type of data (payload) returned in successful responses
 */
@Getter
@Setter
@Builder
public class ApiResponseDto<T> {

    /**
     * Indicates whether the operation was successful (true) or failed (false).
     * This field allows clients to quickly check the result of an API call
     * without inspecting HTTP status codes or error messages.
     */
    private final boolean success;

    /**
     * Generic payload container.
     * Can hold any type of data returned by the API (e.g. entities, DTOs, lists, etc.).
     * This field is typically populated only when 'success' is true.
     */
    private final T data;

    /**
     * <p>
     *     Human-readable message providing about the response.
     * </p>
     * <ul>
     *     <li>For success responses: may contain informational text (e.g. "User created successfully")</li>
     *     <li>For error responses: contains a summary of what went wrong</li>
     * </ul>
     */
    private final String message;

    /**
     * <p>
     *     Optional map of detailed validation or domain errors.
     *     The key usually represents the name of a field or an error category,
     *     and the value can be a String, a List of Strings, or even a nested object
     *     depending on the error structure used in the project.
     * </p>
     *
     * <p>
     *     Example:
     *     {
     *         "email": ["must not be blank", "must be a valid email address"],
     *         "password": ["must contain at least 8 characters"]
     *     }
     * </p>
     */
    private final Map<String, ?> errors;

    /**
     * <p>
     *     Timestamp indicating when the response created.
     *     This is automatically set to the current system time.
     * </p>
     * <p>
     *     It provides useful context for debugging, monitoring and audit logs.
     * </p>
     */
    private final LocalDateTime timeStamp = LocalDateTime.now();

    /**
     * <p>
     *     A unique identifier associated with a specific API request.
     * </p>
     * <p>
     *     This value is typically stored in the MDC (Mapped Diagnostic Context)
     *     and propagated through Log statements for distributed tracing.
     * </p>
     * <p>
     *     It helps developers correlate log entries across different layers
     *     (controller, service, database, etc.) for a single API request.
     * </p>
     */
    private final String correlationId;

    // -------------------------------------------
    // ✅ Factory Methods (Static Builders)
    // -------------------------------------------

    /**
     * Builds a standardized success response.
     *
     * @param data the response payload (can be null if no content)
     * @param correlationId the correlation ID to include for traceability
     * @return a fully initialized {@link ApiResponseDto} with success = true
     */
    public static <T> ApiResponseDto<T> success(T data, String correlationId) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .data(data)
                .message(null)
                .correlationId(correlationId)
                .build();
    }

    /**
     * Builds a standardized error response with detailed validation or domain errors.
     *
     * @param message summary of the error
     * @param errors map containing field-level or business errors
     * @param correlationId the correlation ID to include for traceability
     * @return a fully initialized {@link ApiResponseDto} with success = false
     */
    public static <T> ApiResponseDto<T> error(String message, Map<String, ?> errors, String correlationId) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .errors(errors)
                .correlationId(correlationId)
                .build();
    }

    /**
     * <p>
     *     Builds a simplified error response without detailed error map.
     * </p>
     * <p>
     *     This version is typically used for system or unexpected errors.
     * </p>
     *
     * @param message summary of the error
     * @param correlationId the correlation ID to include for traceability
     * @return a standardized {@link ApiResponseDto} with success = false and no 'errors' map
     * @param <T>
     */
    public static <T> ApiResponseDto<T> error (String message, String correlationId) {
        return error(message, null, correlationId);
    }
}
