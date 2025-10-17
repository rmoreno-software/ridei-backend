package com.ridei.apirest.ridei_apirest.user.validation.validator;

import com.ridei.apirest.ridei_apirest.user.service.UserService;
import com.ridei.apirest.ridei_apirest.user.validation.annotation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator implementation for the {@link UniqueEmail} annotation.
 *
 * <p>This class checks whether a given email address already exists in the system,
 * ensuring that each user has a unique email.</p>
 *
 * <p>The validator uses {@link UserService} to perform the lookup against
 * the database. If the email is already taken, the validation will fail.</p>
 *
 * <p><b>Validation Logic:</b></p>
 * <ul>
 *     <li>If the email value is {@code null}, the validation passes
 *     (to allow other annotations such as {@code @NotBlank} to handle null checks.)</li>
 *     <li>If the email is already present in the database, the validation fails.</li>
 *     <li>If the email is unique, the validation passes.</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 *     public class UserRequest {
 *          @UniqueEmail
 *          private String email;
 *
 *     }
 * }</pre>
 *
 * <p><b>Typical Error Message:</b></p>
 * <ul>
 *     <li>When the email is already taken, a message with key
 *     <code>{user.email.exists}</code> is returned from the resource bundle.</li>
 * </ul>
 *
 * @see UniqueEmail
 * @see ConstraintValidator
 * @see UserService
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    /**
     * Service used to check whether the provided email already exists in the database.
     */
    @Autowired
    private UserService userService;

    /**
     * Validates that the given email is unique.
     *
     * @param email the email value to validate
     * @param context the context in which the constraint is evaluated
     * @return {@code true} if the email is {@code null} or not taken; {@code false} otherwise
     */
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return email == null || !userService.isEmailTaken(email);
    }
}
