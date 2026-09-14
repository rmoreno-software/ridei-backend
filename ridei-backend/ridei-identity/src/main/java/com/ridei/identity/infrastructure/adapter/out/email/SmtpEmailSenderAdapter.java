package com.ridei.identity.infrastructure.adapter.out.email;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
    private final MessageSource messageSource;
    private final String fromAddress;

    public SmtpEmailSenderAdapter(
        JavaMailSender mailSender,
        ITemplateEngine templateEngine,
        MessageSource messageSource,
        @Value("${mail.from-address}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendTemporaryPassword(
        Email to,
        String temporaryPassword,
        Locale locale
    ) {
        Context context = new Context(locale);
        context.setVariable("temporaryPassword", temporaryPassword);
        String html = templateEngine.process("email/temporary-password", context);

        String subject = messageSource.getMessage("email.temp_password.subject", null, locale);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(to.value());
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.addInline("logo", new ClassPathResource("email/ridei_logo_black.png"));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el email de contraseña temporal", e);
        }
    }

    @Override
    public void sendEmailVerificationLink(Email to, String verificationLink, Locale locale) {
        Context context = new Context(locale);
        context.setVariable("verificationLink", verificationLink);
        String html = templateEngine.process("email/verify-email", context);

        String subject = messageSource.getMessage("email.verify.subject", null, locale);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(to.value());
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.addInline("logo", new ClassPathResource("email/ridei_logo_black.png"));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el email de verificación", e);
        }
    }
}
