package com.ridei.identity.application.port.in;

import com.ridei.identity.application.ConfirmProfilePictureCommand;

/**
 * ConfirmProfilePictureUseCase
 */
public interface ConfirmProfilePictureUseCase {
    void confirm(ConfirmProfilePictureCommand command);
}