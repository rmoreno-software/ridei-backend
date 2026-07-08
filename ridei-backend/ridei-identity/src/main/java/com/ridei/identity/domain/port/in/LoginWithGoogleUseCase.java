package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.GoogleAuthResult;
import com.ridei.identity.application.LoginWithGoogleCommand;

public interface LoginWithGoogleUseCase {
    GoogleAuthResult login(LoginWithGoogleCommand command);
}
