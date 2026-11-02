package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.ChangePasswordCommand;
import com.ridei.identity.application.ChangePasswordResult;

public interface ChangePasswordUseCase {
    ChangePasswordResult change (ChangePasswordCommand command);
}
