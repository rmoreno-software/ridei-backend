package com.ridei.identity.domain.model;

public record GoogleUserInfo(
    String googleId,
    String email,
    String firstName,
    String lastName,
    String pictureUrl
) {}
