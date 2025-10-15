package com.ridei.apirest.ridei_apirest.common.exception;

import com.ridei.apirest.ridei_apirest.common.response.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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
 *
 * <p>
 *     Important: this class does not log exception - in production you will usually want to log exceptions
 *     (at least at a debug or error level) and maybe attach a correlation id to the response.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
     *     <li>Builds an {@link ApiResponseDto}</li> with a standard "Validation failed" message and the map of errors</li>
     *     <li>Returns the response with HTTP 400 BAD REQUEST</li>
     * </ol>
     *
     * <p>
     *     Notes & behavior:
     *     <ul>
     *         <li> If multiple validation constraints fail for the same field, {@code getFieldErrors()} returns a list;
     *              the code below uses the last message for each field (subsequent messages overwrite previous ones).
     *              If you need all messages per field, consider storing {@code Map<String, List<String>>} instead.</li>
 *             <li> The current implementation uses {@link FieldError#getDefaultMessage()} which is typically the
     *              message from the constraint. If you use message interpolation & i18n, the message will reflect
     *              the configured message source and locale resolution.</li>
     *     </ul>
     * </p>
     *
     * @param ex the validation exception thrown by Spring when @Valid annotated argument fails validation
     * @return a {@link ResponseEntity} with {@link ApiResponseDto} body and HTTP status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiResponseDto<Void> response = ApiResponseDto.error("Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Captura altres excepcions no controlades
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGeneralException(Exception ex) {
        ApiResponseDto<Void> response = ApiResponseDto.error("Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
