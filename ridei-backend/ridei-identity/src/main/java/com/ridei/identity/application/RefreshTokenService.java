package com.ridei.identity.application;

import com.ridei.identity.domain.exception.InvalidCredentialException;
import com.ridei.identity.domain.exception.UserSuspendedException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.port.in.RefreshTokenUseCase;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class RefreshTokenService implements RefreshTokenUseCase {

    private final JwtPort jwt;
    private final UserRepositoryPort userRepository;

    public RefreshTokenService(
        JwtPort jwt,
        UserRepositoryPort userRepository
    ) {
        this.jwt = jwt;
        this.userRepository = userRepository;
    }

    @Override
    public RefreshTokenResult refresh(RefreshTokenCommand command) {
        String token = command.refreshToken();

        if (!jwt.validateToken(token) || !jwt.isRefreshToken(token))
            throw new InvalidCredentialException();

        UserId userId = jwt.extractUserId(token);
        int tokenVersion = jwt.extractTokenVersion(token);

        User user = userRepository.findById(userId)
            .orElseThrow(InvalidCredentialException::new);

        if (user.getTokenVersion() != tokenVersion)
            throw new InvalidCredentialException();

        if (user.isSuspended())
            throw new UserSuspendedException();

        String accessToken = jwt.generateAccessToken(user.getId(), user.getRole(), user.getTokenVersion());

        return new RefreshTokenResult(accessToken);
    }
}
