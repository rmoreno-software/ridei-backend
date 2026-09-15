package com.ridei.identity.domain.exception;

public class ProfilePictureTooLargeException extends RuntimeException {
    public ProfilePictureTooLargeException() {
        super("error.profile_picture_too_large");
    }
}
