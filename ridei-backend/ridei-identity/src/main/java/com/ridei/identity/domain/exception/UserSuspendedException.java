package com.ridei.identity.domain.exception;

public class UserSuspendedException extends RuntimeException {
    public UserSuspendedException() {
        super("User account is suspended");
    }
}
