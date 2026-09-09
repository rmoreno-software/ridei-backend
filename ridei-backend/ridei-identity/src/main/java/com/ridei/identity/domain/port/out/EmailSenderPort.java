package com.ridei.identity.domain.port.out;

import com.ridei.identity.domain.model.Email;

public interface EmailSenderPort {
    void sendTemporaryPassword(Email to, String temporaryPassword);
    void sendEmailVerificationLink(Email to, String verificationLink);
}
