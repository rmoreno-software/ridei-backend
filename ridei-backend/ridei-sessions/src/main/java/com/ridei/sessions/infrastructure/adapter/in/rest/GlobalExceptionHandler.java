package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.ridei.sessions.domain.exception.ExternalServiceUnavailableException;
import com.ridei.sessions.domain.exception.LapNotFoundException;
import com.ridei.sessions.domain.exception.LayoutNotFoundException;
import com.ridei.sessions.domain.exception.LayoutNotTimeableException;
import com.ridei.sessions.domain.exception.MotorbikeNotFoundException;
import com.ridei.sessions.domain.exception.NoValidLapsException;
import com.ridei.sessions.domain.exception.SessionIdConflictException;
import com.ridei.sessions.domain.exception.SessionNotFoundException;
import com.ridei.sessions.infrastructure.adapter.in.rest.exception.ApiError;
import com.ridei.sessions.infrastructure.adapter.out.http.UpstreamUnauthorizedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
        return status(HttpStatus.BAD_REQUEST, new ApiError(400, "Validation failed", "One or more fields are invalid", details));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedJson(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    @ExceptionHandler({SessionNotFoundException.class, LapNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({LayoutNotFoundException.class, MotorbikeNotFoundException.class, LayoutNotTimeableException.class, NoValidLapsException.class})
    public ResponseEntity<ApiError> handleUnprocessable(RuntimeException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler({SessionIdConflictException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> handleConflict(RuntimeException ex) {
        return error(HttpStatus.CONFLICT, ex instanceof SessionIdConflictException ? ex.getMessage() : "The session was modified concurrently, please retry");
    }

    @ExceptionHandler(UpstreamUnauthorizedException.class)
    public ResponseEntity<ApiError> handleUpstreamUnauthorized(UpstreamUnauthorizedException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleUnavailable(ExternalServiceUnavailableException ex) {
        log.warn("Upstream unavailable: {}", ex.getMessage(), ex.getCause());
        return error(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String message) {
        return status(status, new ApiError(status.value(), status.getReasonPhrase(), message));
    }

    private static ResponseEntity<ApiError> status(HttpStatus status, ApiError body) {
        return ResponseEntity.status(status).body(body);
    }
}
