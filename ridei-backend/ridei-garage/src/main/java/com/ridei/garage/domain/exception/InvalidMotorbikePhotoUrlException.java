package com.ridei.garage.domain.exception;

public class InvalidMotorbikePhotoUrlException extends RuntimeException{
    public InvalidMotorbikePhotoUrlException() {
        super("The given URL does not belong to this motorbike");
    }
}
