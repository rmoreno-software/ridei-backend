package com.ridei.identity.domain.exception;

public class InvalidProfilePictureUrlException extends RuntimeException {
    public InvalidProfilePictureUrlException(String url) {
        super("Profile Picture URL does not belong to the authenticated user: " + url);
    }
}
