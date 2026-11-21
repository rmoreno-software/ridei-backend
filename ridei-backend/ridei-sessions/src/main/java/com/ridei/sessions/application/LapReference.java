package com.ridei.sessions.application;

/** Contra qué se compara una vuelta en el gráfico velocidad–distancia. */
public sealed interface LapReference {

    record Ideal() implements LapReference {}

    record BestLap() implements LapReference {}

    record OtherLap(int number) implements LapReference {}

    /** {@code ideal}, {@code best} o un número de vuelta. */
    static LapReference parse(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("ideal")) return new Ideal();
        if (value.equalsIgnoreCase("best")) return new BestLap();
        try {
            return new OtherLap(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("compareTo must be 'ideal', 'best' or a lap number");
        }
    }
}
