package com.ridei.circuits.domain.model;

/**
 * Datos con los que se da de alta un layout. Solo el nombre es obligatorio: longitud, curvas y
 * sentido los calcula el GPS con la primera sesión, así que pueden llegar a {@code null}
 * (layout pendiente de geometría).
 */
public record LayoutSpec(LayoutName name, Length length, Turns turns, Direction direction) {

    public LayoutSpec {
        if (name == null) throw new IllegalArgumentException("Layout name is required");
    }

    /** Tiene longitud y sentido, lo mínimo para comparar su geometría con la de otro. */
    public boolean hasGeometry() {
        return length != null && direction != null;
    }
}
