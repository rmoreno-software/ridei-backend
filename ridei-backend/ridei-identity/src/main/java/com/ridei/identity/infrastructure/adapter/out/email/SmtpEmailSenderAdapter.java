package com.ridei.identity.infrastructure.adapter.out.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.port.out.EmailSenderPort;

@Component
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailSenderAdapter(
        JavaMailSender mailSender,
        @Value("${mail.from-address}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendTemporaryPassword(
        Email to,
        String temporaryPassword
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to.value());
        message.setSubject("Tu contraseña temporal de Ridei");
        message.setText(
            "Hemos generado una contraseña temporal para tu cuenta:\n\n" +
            temporaryPassword + "\n\n" +
            "Es válida durante 15 minutos. Úsala para iniciar sesión y cámbiala en cuanto entres. \n\n" +
            "Si no has solicitado esto, ignora este correo."
        );
        mailSender.send(message);
    }
}
