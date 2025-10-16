package com.ridei.apirest.ridei_apirest.user.model.dto;

import com.ridei.apirest.ridei_apirest.user.model.Role;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object(DTO) representing a user's information returned by the API.
 *<p>
 *     This class defines the structure og user data sent back to clients after operations
 *     such as user creation, authentication, or retrieval from the database.
 *</p>
 * <p>
 *     Unlike {@link  UserRequest}, this DTO is used exclusively for outbound
 *     data (API responses), and therefore does not include any validation annotations.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 *     {
 *         "id": 1,
 *         "email": "john.doe@example.com",
 *         "firstName": "John",
 *         "lastName": "Doe",
 *         pictureUrl: "https://example.com/images/john.png",
 *         "role": "USER",
 *         "enabled": true,
 *         "createdAt": "2025-10-16T09:15:30"
 *     }
 * </pre>
 *
 * <p><b>Lombok Annotations:</b></p>
 * <ul>
 *     <li>{@link Getter} and {@link Setter} - Automatically generate getters and setters for all fields.</li>
 *     <li>{@link NoArgsConstructor} and {@link AllArgsConstructor} - Provide default and full constructors.</li>
 *     <li>{@link Builder} - Enables the builder pattern for convenient object creation.</li>
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
public class UserResponse {

    /**
     * Unique identifier of the user.
     * <p>Typically generated automatically by the database.</p>
     */
    private Long id;

    /**
     * The user's registered email address.
     * <p>This field is unique and used as the primary login credential.</p>
     */
    private String email;

    /**
     * The user's first name.
     */
    private String firstName;

    /**
     * The user's last name.
     */
    private String lastName;

    /**
     * The URL of the user's profile picture, if available.
     * <p>This may be set manually by the user or retrieved from an external provider (e.g., Google).</p>
     */
    private String pictureUrl;

    /**
     * The user's role within the system.
     * <p>Defines the user's permissions and access level (e.g., USER).</p>
     */
    private Role role;

    /**
     * Indicates whether the user account is active and allowed to authenticate.
     * <p>Disabled users cannot log in to the system</p>
     */
    private boolean enabled;

    /**
     * The timestamp when the user account was created.
     * <p>Automatically populated by the system upon registration.</p>
     */
    private LocalDateTime createdAt;
}
