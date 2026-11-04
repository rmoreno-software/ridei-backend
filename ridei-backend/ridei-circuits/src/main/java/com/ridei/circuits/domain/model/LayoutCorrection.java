package com.ridei.circuits.domain.model;

/** Corrección parcial de un layout: cada campo {@code null} se deja como estaba. */
public record LayoutCorrection(LayoutName name, Length length, Turns turns, Direction direction) {

    LayoutSpec applyTo(LayoutSpec current) {
        return new LayoutSpec(
            name != null ? name : current.name(),
            length != null ? length : current.length(),
            turns != null ? turns : current.turns(),
            direction != null ? direction : current.direction()
        );
    }
}
