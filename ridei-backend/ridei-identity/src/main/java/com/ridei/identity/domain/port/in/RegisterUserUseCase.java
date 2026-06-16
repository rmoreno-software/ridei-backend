package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.RegisterUserCommand;
import com.ridei.identity.domain.model.UserId;

public interface RegisterUserUseCase {
    UserId register(RegisterUserCommand command);
}
