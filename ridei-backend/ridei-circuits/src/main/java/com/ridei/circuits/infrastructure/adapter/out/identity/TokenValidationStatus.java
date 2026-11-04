package com.ridei.circuits.infrastructure.adapter.out.identity;

/** Veredicto de ridei-identity sobre un access token cuya firma ya se ha verificado en local. */
public enum TokenValidationStatus {
    /** Cuenta activa y token vigente (su {@code tv} es el actual). */
    VALID,
    /** Revocado (cambio o restablecimiento de contraseña), cuenta no activa o usuario inexistente. */
    REVOKED,
    /** No se pudo preguntar a identity (caído, timeout, 5xx): no se sabe. */
    UNAVAILABLE
}
