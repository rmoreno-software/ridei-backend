package com.ridei.garage.domain.exception;

public class CannotActivateRetiredMotorbikeException extends RuntimeException {
    public CannotActivateRetiredMotorbikeException() {
        super("A retired motorbike cannot be active");
    }
}
