package com.ridei.identity.domain.exception;

public class UserSuspendedException extends RuntimeException {
    public UserSuspendedException() {
        super("error.user_suspended");
    }
}
