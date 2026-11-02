package com.ridei.garage.domain.exception;

/** La fecha de montaje rompería el orden cronológico del histórico de un componente. */
public class SetupChangeDateConflictException extends RuntimeException {
    public SetupChangeDateConflictException(String message) {
        super(message);
    }
}
