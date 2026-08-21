package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.RegisterUserCommand;
import com.ridei.identity.domain.model.RegisterResult;

public interface RegisterUserUseCase {
    RegisterResult register(RegisterUserCommand command);
}
