package com.ridei.garage.domain.exception;

public class PhotoNotFoundException extends RuntimeException {
    public PhotoNotFoundException() {
        super("Photo not found");
    }
}
