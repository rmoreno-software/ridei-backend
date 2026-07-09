package com.ridei.identity.domain.port.out;

import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserRole;

public interface JwtPort {
    String generateAccessToken(UserId userId, UserRole role);
    String generateRefreshToken(UserId userId);
    boolean validateToken(String token);
    UserId extractUserId(String token);
}
