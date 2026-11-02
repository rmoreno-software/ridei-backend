package com.ridei.garage.domain.exception;

public class SetupEntryNotFoundException extends RuntimeException {
    public SetupEntryNotFoundException() {
        super("Setup entry not found");
    }
}
