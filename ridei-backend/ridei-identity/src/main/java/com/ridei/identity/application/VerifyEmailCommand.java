package com.ridei.identity.application;

public record VerifyEmailCommand(
    String token
) {}
