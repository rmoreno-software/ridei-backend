package com.ridei.identity.infrastructure.adapter.in.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ridei.identity.domain.exception.EmailAlreadyRegisteredException;
import com.ridei.identity.domain.exception.InvalidGoogleTokenException;
import com.ridei.identity.domain.exception.InvalidProfilePictureUrlException;
import com.ridei.identity.domain.exception.MinimumAgeNotMetException;
import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.exception.UserSuspendedException;
import com.ridei.identity.domain.exception.UsernameAlreadyTakenException;
import com.ridei.identity.infrastructure.adapter.in.rest.exception.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -- Errores de validación (@Valid)------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex){
        List<String> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiError(400, "Validation failed", "One or more fields are invalid", details)
            );
    }

    // -- Errores de dominio -----------------------------------------------------------
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ApiError(409, "Conflict", ex.getMessage())
        );
    }

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ResponseEntity<ApiError> handleUsernameAlreadyTaken(UsernameAlreadyTakenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ApiError(409, "Conflict", ex.getMessage())
        );
    }

    @ExceptionHandler(MinimumAgeNotMetException.class)
    public ResponseEntity<ApiError> handleMinimumAge(MinimumAgeNotMetException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            new ApiError(422, "Unprocessable Entity", ex.getMessage())
        );
    }

    // -- Errores de value objects (IllegalArgumentException) --------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiError(400, "Bad Request", ex.getMessage())
        );
    }

    // -- Fallback - cualquier error no controlado ------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ApiError(500, "Internal Server Error", "An unexpected error ocurred")
        );
    }

    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ResponseEntity<ApiError> handleInvalidGoogleToken(InvalidGoogleTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new ApiError(401, "Unaythorized", ex.getMessage())
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiError(401, "Unauthorized", ex.getMessage())
        );
    }

    @ExceptionHandler(UserSuspendedException.class)
    public ResponseEntity<ApiError> handleUserSuspended(UserSuspendedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiError(403, "Forbidden", ex.getMessage())
        );
    }

    @ExceptionHandler(InvalidProfilePictureUrlException.class)
    public ResponseEntity<ApiError> handleInvalidProfilePictureUrl(InvalidProfilePictureUrlException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            new ApiError(403, "Forbidden", ex.getMessage())
        );
    }
}
