package com.ridei.circuits.domain.exception;

/** La fusión pedida no tiene sentido (consigo mismo, disciplinas distintas, layouts de tracks distintos...). */
public class InvalidMergeException extends RuntimeException {

    public InvalidMergeException(String message) {
        super(message);
    }
}
