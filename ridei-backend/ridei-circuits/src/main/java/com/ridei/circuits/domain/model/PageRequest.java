package com.ridei.circuits.domain.model;

/** Página solicitada (base 0). */
public record PageRequest(int page, int size) {

    public static final int MAX_SIZE = 100;

    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("Page must be zero or positive");
        if (size < 1 || size > MAX_SIZE) throw new IllegalArgumentException("Page size must be between 1 and " + MAX_SIZE);
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    public long offset() {
        return (long) page * size;
    }
}
