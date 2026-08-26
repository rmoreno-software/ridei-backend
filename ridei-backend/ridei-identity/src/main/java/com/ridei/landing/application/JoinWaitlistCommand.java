package com.ridei.landing.application;

import com.ridei.landing.domain.model.Email;

public record JoinWaitlistCommand(
    Email email
) {}
