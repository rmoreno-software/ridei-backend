package com.ridei.apirest.ridei_apirest.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * Configuration class for internationalization (i18n) and validation messages.
 *
 * <p>This class provides beans to handle locale resolution, message sources for multiple
 * languages, and integration with Bean Validation to support localized validation messages.</p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *     <li>Define a {@link LocaleResolver} that reads the "Accept-Language" header from HTTP requests
 *         and falls back to English if no locale is specified.</li>
 *     <li>Define a {@link MessageSource} that loads messages from properties files
 *         (e.g., messages.properties, messages_es.properties) and supports UTF-8 encoding.</li>
 *     <li>Define a {@link LocalValidatorFactoryBean} that integrates Bean Validation with the
 *         {@link MessageSource} for localized validation messages (e.g., @NotBlank, @Size).</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * // Example of validation message in messages.properties
 * user.email.required=Email is required
 *
 * // Example of validation message in messages_es.properties
 * user.email.required=El correo electrónico es obligatorio
 *
 * // The LocalValidatorFactoryBean ensures that @Valid annotations use these messages
 * </pre>
 *
 * <p>With this configuration, all controllers using {@code @Valid} will return validation
 * errors in the language requested by the client, falling back to English if no language
 * is specified.</p>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Configuration
public class MessageConfig {

    /**
     * Creates a {@link LocaleResolver} that determines the locale of incoming requests
     * based on the "Accept-Language" HTTP header.
     *
     * <p>If the client does not specify a language, English (en) is used by default.</p>
     *
     * @return a configured {@link LocaleResolver} instance
     */
    @Bean
    public LocaleResolver localeResolver() {
        // Create a resolver that uses the Accept-Language HTTP header
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();

        // Define the fallback (English) when no Accept-Language header is provided
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    /**
     * Creates a {@link MessageSource} that loads message bundles for i18n.
     *
     * <p>This message source supports:
     * <ul>
     *     <li>UTF-8 encoding for accented and non-Latin characters</li>
     *     <li>Multiple languages through properties files named "messages*.properties"</li>
     *     <li>Automatic fallback to English when no translation exists</li>
     * </ul>
     * </p>
     *
     * @return a configured {@link MessageSource} instance
     */
    @Bean
    public MessageSource messageSource() {
        // Create a reloadable message source that supports UTF-8 and multiple languages
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        // Set the base name for messages bundles (e.g., messages.properties, messages_es.properties, etc.)
        messageSource.setBasename("classpath:messages");

        // Ensure proper UTF-8 encoding for accented and non-Latin characters
        messageSource.setDefaultEncoding("UTF-8");

        // Define English as the default Language
        messageSource.setDefaultLocale(Locale.ENGLISH);
        return messageSource;
    }

    /**
     * Creates a {@link LocalValidatorFactoryBean} that integrates Bean Validation with the
     * {@link MessageSource} for localized validation messages.
     *
     * <p>All @Valid annotations on DTOs will use this message source to return errors
     * in the appropriate language based on the client's request.</p>
     *
     * @param messageSource the {@link MessageSource} used for validation messages
     * @return a configured {@link LocalValidatorFactoryBean} instance
     */
    @Bean
    public LocalValidatorFactoryBean getValidator(MessageSource messageSource) {
        // Create a factory bean that integrates Bean Validation with our i18n message source
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();

        // Set the source of localized validation messages
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}
