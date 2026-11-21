package com.ridei.sessions.domain.exception;


public class LayoutNotFoundException extends RuntimeException {
    public LayoutNotFoundException() {
        super("Layout not found in the circuits catalog");
    }
}
