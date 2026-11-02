package com.ridei.identity.domain.port.in;

import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserProfile;

public interface GetCurrentUserUseCase {
    UserProfile get(UserId userId);
}
