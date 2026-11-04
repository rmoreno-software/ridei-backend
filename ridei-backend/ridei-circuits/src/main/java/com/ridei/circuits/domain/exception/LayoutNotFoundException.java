package com.ridei.circuits.domain.exception;

import com.ridei.circuits.domain.model.LayoutId;

public class LayoutNotFoundException extends RuntimeException {

    public LayoutNotFoundException(LayoutId id) {
        super("Layout not found: " + id);
    }
}
