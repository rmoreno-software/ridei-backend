package com.ridei.garage.domain.exception;

public class MotorbikeNotFoundException extends RuntimeException {
    public MotorbikeNotFoundException() {
        super("Motorbike not found");
    }
}
