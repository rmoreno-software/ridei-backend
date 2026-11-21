package com.ridei.sessions.domain.exception;


public class MotorbikeNotFoundException extends RuntimeException {
    public MotorbikeNotFoundException() {
        super("Motorbike not found in your garage");
    }
}
