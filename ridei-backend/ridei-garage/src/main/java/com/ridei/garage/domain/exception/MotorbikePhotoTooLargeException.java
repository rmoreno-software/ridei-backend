package com.ridei.garage.domain.exception;

public class MotorbikePhotoTooLargeException extends RuntimeException {
    public MotorbikePhotoTooLargeException() {
        super("Photo exceeds the maximum allowed size");
    }
}
