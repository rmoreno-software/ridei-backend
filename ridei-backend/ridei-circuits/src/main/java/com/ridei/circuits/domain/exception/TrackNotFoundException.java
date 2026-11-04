package com.ridei.circuits.domain.exception;

import com.ridei.circuits.domain.model.TrackId;

public class TrackNotFoundException extends RuntimeException {

    public TrackNotFoundException(TrackId id) {
        super("Track not found: " + id);
    }
}
