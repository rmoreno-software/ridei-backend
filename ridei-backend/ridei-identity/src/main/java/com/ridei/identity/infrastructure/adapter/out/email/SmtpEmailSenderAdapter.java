package com.ridei.identity.infrastructure.adapter.out.email;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.port.out.EmailSenderPort;

import jakarta.mail.internet.MimeMessage;

@Component
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final String fromAddress;

    public SmtpEmailSenderAdapter(
        JavaMailSender mailSender,
        ITemplateEngine templateEngine,
        @Value("${mail.from-address}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendTemporaryPassword(
        Email to,
        String temporaryPassword
    ) {
        Context context = new Context();
        context.setVariable("temporaryPassword", temporaryPassword);
        String html = templateEngine.process("email/temporary-password", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(to.value());
            helper.setSubject("Tu contraseña temporal de Ridei");
            helper.setText(html, true);
            helper.addInline("logo", new ClassPathResource("email/ridei_logo_black.png"));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el email de contraseña temporal", e);
        }
    }

    @Override
    public void sendEmailVerificationLink(Email to, String verificationLink) {
        Context context = new Context();
        context.setVariable("verificationLink", verificationLink);
        String html = templateEngine.process("email/verify-email", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(to.value());
            helper.setSubject("Verifica tu email de Ridei");
            helper.setText(html, true);
            helper.addInline("logo", new ClassPathResource("email/ridei_logo_black.png"));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el email de verificación", e);
        }
    }
}
