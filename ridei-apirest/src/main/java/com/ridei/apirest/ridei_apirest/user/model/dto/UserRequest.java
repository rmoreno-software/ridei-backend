package com.ridei.apirest.ridei_apirest.user.model.dto;

import com.ridei.apirest.ridei_apirest.user.validation.annotation.UniqueEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Data Transfer Object (DTO) representing a user creation or registration request.
 * <p>
 *     This class defines the structure of the JSON body expected by the user creation
 *     endpoint ({@code Post /api/users}). It includes validation annotations to ensure
 *     that the provided input data meets business and formatting requirements.
 * </p>
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *     <li><b>Email:</b> Required, must be a valid format, and unique in the system.</li>
 *     <li><b>First Name:</b> Required, 2-50 characters.</li>
 *     <li><b>Last Name:</b> Required, 2-50 characters.</li>
 *     <li><b>Password:</b> Required, minimum 6 characters.</li>
 * </ul>
 *
 * <p><b>Internationalization (i18n):</b></p>
 * All validation message are externalized in message bundles (e.g. <code>messages.properties</code>)
 * using keys such as <code>user.email.required</code> or <code>user.password.size</code>.
 * This allows displaying error messages in multiple languages depending on the configured locale.
 *
 * <p><b>Example JSON Request:</b></p>
 * <pre>
 *     {
 *         "email": "user@example.com",
 *         "firstName": "John",
 *         "lastName": "Doe",
 *         "password": "securePass123"
 *     }
 * </pre>
 *
 * <p><b>Annotations:</b></p>
 * <ul>
 *     <li>{@link Schema} - Provides metadata for API documentation (Swagger/OpenAPI).</li>
 *     <li>{@link NotBlank}, {@link Email}, {@link Size} - Define validation constraint.</li>
 *     <li>{@link UniqueEmail} - Custom validation to ensure email uniqueness in the database.</li>
 *     <li>{@link Getter}, {@link Setter}, {@link Builder}, {@link NoArgsConstructor}, {@link AllArgsConstructor} - Lombok annotations to reduce boilerplate code.</li>
 * </ul>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    /**
     * The email address of the user.
     * <ul>
     *     <li>Must not be blank</li>
     *     <li>Must be a valid email format</li>
     *     <li>Must be unique across the system</li>
     * </ul>
     *
     * <p>Validation messages are resolved from <code>message.properties</code> using the key
     * <code>user.email.required</code>.</p>
     */
    @Schema(description = "User email", example = "usuario@dominio.com")
    @NotBlank(message = "{user.email.required}")
    @Email(message = "{user.email.required}")
    @UniqueEmail
    private String email;

    /**
     * The first name of the user.
     * <ul>
     *     <li>Must not be blank</li>
     *     <li>Must contain between 2 and 50 characters</li>
     * </ul>
     *
     * <p>Validation messages are localized using keys such as
     * <code>user.firstname.required</code> and <code>user.firstname.size</code>.</p>
     */
    @Schema(description = "User name", example = "Juan")
    @NotBlank(message = "{user.firstname.required}")
    @Size(min = 2, max = 50, message = "{user.firstname.size}")
    private String firstName;

    /**
     * The last name of the user.
     * <ul>
     *     <li>Must not be blank</li>
     *     <li>Must contain between 2 and 50 characters</li>
     * </ul>
     *
     * <p>Validation messages are localized using keys such as
     * <code>user.lastname.required</code> and <code>user.lastname.size</code>.</p>
     */
    @Schema(description = "User lastname", example = "López")
    @NotBlank(message = "{user.lastname.required}")
    @Size(min = 2, max = 50, message = "{user.lastname.size}")
    private String lastName;

    /**
     * The password for the user account.
     * <ul>
     *     <li>Must not be blank</li>
     *     <li>Must contain at least 6 characters</li>
     * </ul>
     *
     * <p>Validation messages are localized using keys such as
     * <code>user.password.required</code> and <code>user.password.size</code>.</p>
     */
    @Schema(description = "User password.", example = "123456")
    @NotBlank(message = "{user.password.required}")
    @Size(min = 6, message = "{user.password.size}")
    private String password;

}
