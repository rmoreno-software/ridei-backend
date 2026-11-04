package com.ridei.identity.domain.model;

/**
 * Rol de la cuenta, emitido en el claim {@code role} del access token. {@code ADMIN} lo
 * consumen otros servicios (p. ej. ridei-circuits) para sus comandos de administración;
 * no se asigna desde ninguna API: se concede a mano en la base de datos.
 */
public enum UserRole {
    RIDER,
    ORGANIZER,
    ADMIN
}
