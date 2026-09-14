package com.ridei.identity.domain.exception;

import com.ridei.identity.domain.model.Username;

public class UsernameAlreadyTakenException extends RuntimeException {
    public UsernameAlreadyTakenException(Username username) {
        super("error.username_already_taken");
    }
}
