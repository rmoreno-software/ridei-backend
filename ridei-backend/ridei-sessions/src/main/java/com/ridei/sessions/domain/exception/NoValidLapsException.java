package com.ridei.sessions.domain.exception;


public class NoValidLapsException extends RuntimeException {
    public NoValidLapsException() {
        super("This session has no valid laps to analyse");
    }
}
