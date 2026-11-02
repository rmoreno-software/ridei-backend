package com.ridei.identity.application.port.in;

import com.ridei.identity.application.RemoveProfilePictureCommand;

public interface RemoveProfilePictureUseCase {
    void remove(RemoveProfilePictureCommand command);
}
