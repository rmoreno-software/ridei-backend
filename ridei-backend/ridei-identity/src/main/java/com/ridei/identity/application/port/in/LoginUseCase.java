package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.LoginCommand;
import com.ridei.identity.application.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
