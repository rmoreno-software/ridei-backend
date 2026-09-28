package com.ridei.garage.domain.exception;

public class BrandNotFoundException extends RuntimeException {
    public BrandNotFoundException() {
        super("Brand not found");
    }
}
