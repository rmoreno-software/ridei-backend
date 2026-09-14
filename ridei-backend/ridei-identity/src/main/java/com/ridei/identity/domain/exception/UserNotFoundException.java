package com.ridei.identity.domain.exception;

import com.ridei.identity.domain.model.UserId;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UserId userId) {
        super("error.user_not_found");
    }
}
