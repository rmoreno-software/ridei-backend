package com.ridei.identity.infrastructure.adapter.in.web.dto;

/**
 * Standardized Data Transfer Object (DTO) for all API responses.
 * <p>
 * This record implements the <b>Envelope Pattern</b>, ensuring that API clients always
 * receive a consistent JSON structure containing metadata (status code, message)
 * and the actual payload (data). This consistency simplifies error handling and
 * parsing on the client side (Frontend/Mobile).
 * </p>
 *
 * @param <T> The type of the payload data. Use {@link Void} if no data is returned.
 * @param code    The HTTP status code (e.g., 200, 400, 500). Included in the body
 * to allow easier parsing by clients that might abstract away HTTP headers.
 * @param message A human-readable message describing the result (e.g., "Success", "Invalid Email").
 * @param data    The actual business data payload. Can be {@code null} for errors or void responses.
 */
public record ApiResponse<T>(
    int code,
    String message,
    T data
) {  

    /**
     * Factory method for successful operations without a response body.
     * <p>
     * Useful for operations like Registration (201 Created) or Delete (204 No Content).
     * </p>
     *
     * @param code    The HTTP status code.
     * @param message The success message.
     * @return An {@code ApiResponse} with null data.
     */
    public static ApiResponse<Void> success(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * Factory method for successful operations containing data.
     *
     * @param code    The HTTP status code.
     * @param message The success message.
     * @param data    The payload object (e.g., UserDTO, Token).
     * @param <T>     The type of the data.
     * @return An {@code ApiResponse} containing the requested data.
     */
    public static <T> ApiResponse<T> success(int code, String meString, T data) {
        return new ApiResponse<T>(code, meString, data);
    }

    /**
     * Factory method for error responses.
     * <p>
     * <b>Professional Practice:</b> Separating 'success' from 'error' semantics makes
     * the code in the ExceptionHandler much more readable.
     * </p>
     *
     * @param code    The error status code (e.g., 400, 404, 500).
     * @param message The error description.
     * @return An {@code ApiResponse} with null data.
     */
    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
