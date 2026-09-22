package com.ridei.identity.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ridei.identity.domain.exception.EmailAlreadyRegisteredException;
import com.ridei.identity.domain.exception.InvalidCredentialException;
import com.ridei.identity.domain.exception.InvalidGoogleTokenException;
import com.ridei.identity.domain.exception.InvalidOrExpiredVerificationTokenException;
import com.ridei.identity.domain.exception.InvalidProfilePictureUrlException;
import com.ridei.identity.domain.exception.MinimumAgeNotMetException;
import com.ridei.identity.domain.exception.ProfilePictureTooLargeException;
import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.exception.UserSuspendedException;
import com.ridei.identity.domain.exception.UsernameAlreadyTakenException;
import com.ridei.identity.infrastructure.adapter.in.rest.exception.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private String translate(String code, Locale locale) {
        return messageSource.getMessage(code, null, code, locale);
    }

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
    public ResponseEntity<ApiError> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ApiError(409, "Conflict", translate(ex.getMessage(), locale))
        );
    }

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ResponseEntity<ApiError> handleUsernameAlreadyTaken(UsernameAlreadyTakenException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ApiError(409, "Conflict", translate(ex.getMessage(), locale))
        );
    }

    @ExceptionHandler(MinimumAgeNotMetException.class)
    public ResponseEntity<ApiError> handleMinimumAge(MinimumAgeNotMetException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            new ApiError(422, "Unprocessable Entity", translate(ex.getMessage(), locale))
        );
    }

    // -- Errores de value objects (IllegalArgumentException) --------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiError(400, "Bad Request", translate(ex.getMessage(), locale))
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
    public ResponseEntity<ApiError> handleInvalidGoogleToken(InvalidGoogleTokenException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new ApiError(401, "Unauthorized", translate(ex.getMessage(), locale))
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiError(401, "Unauthorized", translate(ex.getMessage(), locale))
        );
    }

    @ExceptionHandler(UserSuspendedException.class)
    public ResponseEntity<ApiError> handleUserSuspended(UserSuspendedException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiError(403, "Forbidden", translate(ex.getMessage(), locale))
        );
    }

    @ExceptionHandler(InvalidProfilePictureUrlException.class)
    public ResponseEntity<ApiError> handleInvalidProfilePictureUrl(InvalidProfilePictureUrlException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            new ApiError(403, "Forbidden", translate(ex.getMessage(), locale))
        );
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ApiError> handleInvalidCredential(InvalidCredentialException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new ApiError(401, "Unauthorized", translate(ex.getMessage(), locale))
        );
    }

    @ExceptionHandler(InvalidOrExpiredVerificationTokenException.class)
    public ResponseEntity<ApiError> handleInvalidOrExpiredVerificationTokenException(InvalidOrExpiredVerificationTokenException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiError(400, "Bad Request", translate(ex.getMessage(), locale))
        );
    }

    @ExceptionHandler(ProfilePictureTooLargeException.class)
    public ResponseEntity<ApiError> handleProfilePictureTooLargeException(ProfilePictureTooLargeException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
            new ApiError(413, "Payload Too Large", translate(ex.getMessage(), locale))
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiError(400, "Bad Request", "Malformed request body")
        );
    }
}
