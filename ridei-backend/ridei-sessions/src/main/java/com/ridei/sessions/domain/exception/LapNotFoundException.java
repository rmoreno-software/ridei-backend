package com.ridei.sessions.domain.exception;


public class LapNotFoundException extends RuntimeException {
    public LapNotFoundException() {
        super("Lap not found in this session");
    }
}
