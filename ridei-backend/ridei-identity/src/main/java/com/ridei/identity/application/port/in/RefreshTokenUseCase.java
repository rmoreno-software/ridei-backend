package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.RefreshTokenCommand;
import com.ridei.identity.application.RefreshTokenResult;

public interface RefreshTokenUseCase {
    RefreshTokenResult refresh(RefreshTokenCommand command);
}
