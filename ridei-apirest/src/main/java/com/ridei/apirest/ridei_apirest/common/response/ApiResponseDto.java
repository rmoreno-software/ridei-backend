package com.ridei.apirest.ridei_apirest.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic DTO for API responses in the Ridei REST API.
 *
 * <p>This class provides a standardized structure for all responses returned by
 * controllers, including success/failure status, optional data, error details,
 * timestamps, and correlation IDs for request tracing.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li>Indicates whether the request was successful via {@code success} flag.</li>
 *     <li>Holds the response data in the generic {@code data} field.</li>
 *     <li>Provides an optional {@code message} field for human-readable status or error description.</li>
 *     <li>Supports detailed validation or business errors using {@code errors} map.</li>
 *     <li>Includes {@code timeStamp} for when the response was created.</li>
 *     <li>Includes a {@code correlationId} for tracing requests across logs and services.</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 *      // Successful response with data
 *      ApiResponseDto<UserResponse>; response = ApiResponseDto.success(userDto, correlationId);
 *
 *      // Error response with message and validation errors
 *      Map<String, List<String>> errors = Map.of("email", List.of("Email is required"));
 *      ApiResponseDto<Void> response = ApiResponseDto.error("Validation failed", errors, correlationId);
 *
 *      // Error response with only message
 *      ApiResponseDto<Void>; response = ApiResponseDto.error("Internal Server Error", correlationId);
 * }
 * </pre>
 *
 * <p>This DTO is commonly used in conjunction with {@link com.ridei.apirest.ridei_apirest.common.exception.GlobalExceptionHandler}
 * and controllers to provide consistent API responses.</p>
 *
 * @param <T> the type of data contained in the response
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Getter
@Setter
@Builder
public class ApiResponseDto<T> {

    /**
     * Indicates whether the API call was successful.
     */
    private final boolean success;

    /**
     * The actual data returned by the API in case of success.
     */
    private final T data;

    /**
     * Human-readable message describing the response or error.
     */
    private final String message;

    /**
     * Optional map of field-specific or general errors.
     * The key is the field name, and the value is the corresponding error(s).
     */
    private final Map<String, ?> errors;

    /**
     * Timestamp representing when this response object was created.
     */
    private final LocalDateTime timeStamp = LocalDateTime.now();

    /**
     * Correlation ID used for tracing requests across logs and distributed systems.
     */
    private final String correlationId;

    /**
     * Creates a successful API response containing the given data.
     *
     * @param data the response payload
     * @param correlationId correlation ID for tracing
     * @param <T> type of response data
     * @return an ApiResponseDto representing success
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
     * Creates an error API response with a message and optional error details.
     *
     * @param message human-readable error message
     * @param errors optional map of errors (e.g., validation errors)
     * @param correlationId correlation ID for tracing
     * @param <T> type of response data (usually Void for errors)
     * @return an ApiResponseDto representing the error
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
     * Creates an error API response with only a message.
     *
     * @param message human-readable error message
     * @param correlationId correlation ID for tracing
     * @param <T> type of response data (usually Void for errors)
     * @return an ApiResponseDto representing the error
     */
    public static <T> ApiResponseDto<T> error (String message, String correlationId) {
        return error(message, null, correlationId);
    }
}
