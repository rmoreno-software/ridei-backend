package com.ridei.garage.domain.model;

/**
 * Neumático montado: modelo comercial (p. ej. "Michelin Power Cup 2") y medida opcional.
 * La medida es texto libre a propósito: asfalto ("180/55 ZR17"), off-road ("110/90-19")
 * y clásicas ("3.00-21") no comparten formato.
 */
public record TyreSpec(String model, String size) implements SetupSpec {

    public static final int MAX_MODEL_LENGTH = 100;
    public static final int MAX_SIZE_LENGTH = 30;

    public TyreSpec {
        if (model == null || model.isBlank())
            throw new IllegalArgumentException("Tyre model is required");
        model = model.trim();
        if (model.length() > MAX_MODEL_LENGTH)
            throw new IllegalArgumentException("Tyre model must be at most " + MAX_MODEL_LENGTH + " characters");

        size = (size == null || size.isBlank()) ? null : size.trim();
        if (size != null && size.length() > MAX_SIZE_LENGTH)
            throw new IllegalArgumentException("Tyre size must be at most " + MAX_SIZE_LENGTH + " characters");
    }
}
