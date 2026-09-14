package com.ridei.identity.domain.exception;

public class InvalidProfilePictureUrlException extends RuntimeException {
    public InvalidProfilePictureUrlException(String url) {
        super("error.invalid_profile_picture_url");
    }
}
