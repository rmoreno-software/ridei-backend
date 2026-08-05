package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.RemoveProfilePictureCommand;

public interface RemoveProfilePictureUseCase {
    void remove(RemoveProfilePictureCommand command);
}
