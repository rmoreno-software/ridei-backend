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
 * Central configuration class responsible for application-wide internationalization (i18n)
 * and message handling.
 * <p>
 *     This configuration ensures that:
 *     <ul>
 *         <li>All validation and API messages are translated based on the user's local.</li>
 *         <li>Localization is automatically determined using the <b>Accept-Language</b> HTTP header.</li>
 *         <li>English is used as the default language if no locale is specified by the client.</li>
 *         <li>Spring Validation annotations (e.g. {@code @NotBlank}, {@code @Email}) and custom
 *             exceptions can use messages from properties files (e.g., {@code messages_ca.properties}).</li>
 *     </ul>
 *
 *     <p>
 *         Expected message bundle files:
 *         <ul>
 *             <li>{@code src/main/resources/messages.properties} ➡ Default (English)</li>
 *             <li>{@code src/main/resources/messages_ca.properties} ➡ Catalan</li>
 *             <li>{@code src/main/resources/messages_es.properties} ➡ Spanish</li>
 *         </ul>
 *     </p>
 * </p>
 *
 * These messages can be referenced in validation annotations or service exceptions.
 */
@Configuration
public class MessageConfig {

    /**
     * Defines the default {@link LocaleResolver}.
     * <p>
     *     This resolver determines the locale (language and region) based on the
     *     {@code Accept-Language} HTTP header sent by the client.
     *     If the client does not specify locale, the default language will be English (en)
     * </p>
     *
     * @return a configured {@link LocaleResolver} that uses HTTP headers to determine language preference.
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
     * Configures the application's {@link MessageSource}, which provides localized messages
     * for validations, exceptions, and API responses.
     * <p>
     *     This implementation uses {@link ReloadableResourceBundleMessageSource}, allowing message
     *     bundles to be reloaded automatically during development without restarting the application.
     * </p>
     * <p>
     *     Characteristics:
     *     <ul>
     *         <li>Loads message files with the base name {@code messages}</li>
     *         <li>Supports multiple locales (e.g., English, Spanish, Catalan)</li>
     *         <li>Encodes all files as UTF-8 support special characters</li>
     *         <li>Defaults to English when no locale is specified</li>
     *     </ul>
     * </p>
     *
     * @return a {@link MessageSource} bean responsible for resolving localized text from message bundles.
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
     * Configures a costum validator factory to integrate Bean Validation (JSR 380)
     * with the configured {@link MessageSource}.
     * <p>
     *     This allows validation annotations like {@code @NotBlank}, {@code @Size}, or custom ones
     *     (e.g., {@code @UniqueEmail}) to use localized messages defined in
     *     {@code message_*.properties} files instead of hardcoded English text.
     * </p>
     * For example:
     * <pre>
     *     @NotBlank(message = "{user.email.required}")
     *     private String email;
     * </pre>
     *
     * @param messageSource the configured {@link MessageSource} bean.
     * @return a {@link LocalValidatorFactoryBean} that supports internationalized validation messages.
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
