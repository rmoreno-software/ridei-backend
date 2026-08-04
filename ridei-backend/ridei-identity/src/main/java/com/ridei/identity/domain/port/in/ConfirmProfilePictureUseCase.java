package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.ConfirmProfilePictureCommand;

/**
 * ConfirmProfilePictureUseCase
 */
public interface ConfirmProfilePictureUseCase {
    void confirm(ConfirmProfilePictureCommand command);
}