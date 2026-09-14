package com.ridei.identity.application;

import java.util.Locale;

public record RequestTemporaryPasswordCommand(String email, Locale locale) {}
