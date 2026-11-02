package com.ridei.identity.application.port.in;

import com.ridei.identity.application.RegisterResult;
import com.ridei.identity.application.RegisterUserCommand;

public interface RegisterUserUseCase {
    RegisterResult register(RegisterUserCommand command);
}
