package com.ridei.apirest.ridei_apirest.user.validation.annotation;

import com.ridei.apirest.ridei_apirest.user.validation.validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation that ensures a user's email address is unique within the system.
 *
 * <p>This annotation is applied to a field (typically {@code email}) in a DTO or entity
 * and is validated by {@link UniqueEmailValidator}, which performs the actual check
 * against the database.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 *     public class UserRequest {
 *
 *          @UniqueEmail
 *          private String email;
 *
 *     }
 * }</pre>
 *
 * <p><b>How it works:</b></p>
 * <ul>
 *     <li>The annotation triggers the {@link UniqueEmailValidator} at runtime.</li>
 *     <li>The validator calls the {@code UserService} or {@code UserRepository}
 *     to verify if the email already exists in the database.</li>
 *     <li>If a duplicate email is found, a validation error is raised
 *     with the message key defined in {@code message()}.</li>
 * </ul>
 *
 * <p><b>Internationalization (i18n):</b></p>
 * <ul>
 *     <li>The default message key is <code>{user.email.exists}</code>.</li>
 *     <li>This key should be defined in <code>message.properties</code> files
 *     for multilingual support (e.g., English, Spanish, Catalan, etc.).</li>
 * </ul>
 *
 * @see UniqueEmailValidator
 * @see Constraint
 * @see com.ridei.apirest.ridei_apirest.user.model.dto.UserRequest
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Documented
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {

    /**
     * Defines the default message key for validation errors.
     *
     * <p>This message is resolved from the <code>messages.properties</code> file
     * using the current request locale. If not found, the key itself is displayed.</p>
     *
     * @return the message key or literal message text
     */
    String message() default "{user.email.exists}";

    /**
     * Allows grouping of multiple validation constraints.
     *
     * <p>This rarely used in standard validations but can be helpful
     * when applying validation groups for specific API contexts.</p>
     *
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Can be used by clients to assign custom payload objects to a constraint.
     *
     * <p>This feature is mostly used by advanced validation frameworks
     * for metadata or severity levels.</p>
     *
     * @return custom payload classes associated with this constraint
     */
    Class<? extends Payload>[] payload() default {};
}
