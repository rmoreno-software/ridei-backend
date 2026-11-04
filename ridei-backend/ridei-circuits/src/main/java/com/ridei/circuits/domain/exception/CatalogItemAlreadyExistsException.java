package com.ridei.circuits.domain.exception;

/**
 * Una restricción UNIQUE de la base de datos rechazó la escritura: otra transacción creó
 * el mismo elemento a la vez. Es la última barrera frente a condiciones de carrera.
 */
public class CatalogItemAlreadyExistsException extends RuntimeException {

    public CatalogItemAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
