package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.RegisterResult;
import com.ridei.identity.application.RegisterUserCommand;

public interface RegisterUserUseCase {
    RegisterResult register(RegisterUserCommand command);
}
