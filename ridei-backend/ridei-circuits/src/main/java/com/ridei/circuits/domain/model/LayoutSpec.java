package com.ridei.circuits.domain.model;

/** Datos con los que se da de alta un layout. {@code turns} es opcional. */
public record LayoutSpec(LayoutName name, Length length, Turns turns, Direction direction) {

    public LayoutSpec {
        if (name == null) throw new IllegalArgumentException("Layout name is required");
        if (length == null) throw new IllegalArgumentException("Layout length is required");
        if (direction == null) throw new IllegalArgumentException("Layout direction is required");
    }
}
