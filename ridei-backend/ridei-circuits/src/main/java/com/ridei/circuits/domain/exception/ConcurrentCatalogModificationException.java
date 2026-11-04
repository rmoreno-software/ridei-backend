package com.ridei.circuits.domain.exception;

/** El agregado cambió en otra transacción entre la lectura y la escritura (bloqueo optimista). */
public class ConcurrentCatalogModificationException extends RuntimeException {

    public ConcurrentCatalogModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
