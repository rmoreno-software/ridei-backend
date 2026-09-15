package com.ridei.identity.domain.port.out;

import java.util.Locale;

import com.ridei.identity.domain.model.Email;

public interface EmailSenderPort {
    void sendTemporaryPassword(Email to, String temporaryPassword, Locale locale);
    void sendEmailVerificationLink(Email to, String verificationLink, Locale locale);
    void sendPasswordChangedNotification(Email to, Locale locale);
}
