package com.ridei.identity.domain.port.out;

import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserRole;

public interface JwtPort {
    String generateAccessToken(UserId userId, UserRole role, int tokenVersion);
    String generateRefreshToken(UserId userId, int tokenVersion);
    boolean validateToken(String token);
    UserId extractUserId(String token);
    String extractRole(String token);
    boolean isRefreshToken(String token);
    int extractTokenVersion(String token);
}
