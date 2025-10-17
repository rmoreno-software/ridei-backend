package com.ridei.apirest.ridei_apirest.user.model.entity;

import com.ridei.apirest.ridei_apirest.user.model.AuthenticationProvider;
import com.ridei.apirest.ridei_apirest.user.model.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity class that represents an application user stored in the database.
 * <p>
 *     This entity combines both local authentication (email/password)
 *     and external authentication (e.g., Google OAuth2) into single user model.
 * </p>
 * <p>
 *     The class also handles audit information such as creation and update timestamp,
 *     and includes role-based access control information.
 * </p>
 *
 * <p><b>Database Table:</b> {@code users}</p>
 *
 * <p><b>Lombok Annotations:</b></p>
 * <ul>
 *   <li>{@link Getter} and {@link Setter} – Automatically generate getters and setters.</li>
 *   <li>{@link NoArgsConstructor} and {@link AllArgsConstructor} – Provide constructors for flexible instantiation.</li>
 *   <li>{@link Builder} – Enables the builder pattern for object creation.</li>
 * </ul>
 *
 * <p><b>JPA Annotations:</b></p>
 * <ul>
 *     <li>{@link Entity} - Marks this class as a JPA entity.</li>
 *     <li>{@link  Table} - Defines the table name as "users".</li>
 *     <li>{@link Id}, {@link GeneratedValue} - Configure the primary key strategy.</li>
 *     <li>{@link Enumerated} - Persists enums as strings in the database for readability.</li>
 *     <li>{@link PrePersist}, {@link PreUpdate} - Automatically manage timestamps for entity lifecycle events.</li>
 * </ul>
 *
 * <pre>
 *  UserApp user = UserApp.builder()
 *    .email("john.doe@example.com")
 *    .password("encrypted-password")
 *    .firstName("John")
 *    .lastName("Doe")
 *    .provider(AuthenticationProvider.LOCAL)
 *    .role(Role.USER)
 *    .enabled(true)
 *    .build();
 * </pre>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApp {

    /**
     * Unique identifier for the user.
     * <p>Generated automatically by the database using an identity strategy.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Authentication Data ---

    /**
     * The user's email address.
     * <p>Must be unique and not null. Used as the main login credential.</p>
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * The user's encrypted password.
     * <p>Required only for local authentication (not for Google login)<./p>
     */
    @Column(nullable = false)
    private String password;

    // --- Google Authentication Data ---

    /**
     * The unique identifier provided by Google for OAuth2 authentication.
     * <p>This field is only populated when the user registers or logs in with Google.</p>
     */
    private String googleId;  // ID únic proporcionat per google

    /**
     * URL of the user's Google profile picture (avatar).
     * <p>Can be updated manually or retrieved automatically from Google.</p>
     */
    private String pictureUrl; // URL del avatar de Google

    /**
     * The authentication provider used by the user.
     * <p>Can be {@link AuthenticationProvider#GOOGLE} or {@link AuthenticationProvider#LOCAL}.</p>
     */
    @Enumerated(EnumType.STRING)
    private AuthenticationProvider provider; // "google" o Local

    // --- Personal Information ---

    /**
     * The user's first name.
     */
    private String firstName;

    /**
     * The user's last name.
     */
    private String lastName;

    // -- Status and Audit Fields --

    /**
     * Indicates whether the account is active and can access the system.
     * <p>Default value is {@code true}</p>
     */
    private boolean enabled = true;

    /**
     * Indicates whether the user's email address has been verified.
     * <p>Used for account confirmation in registration workflows.</p>
     */
    private boolean emailVerified = false;

    /**
     * Timestamp marking when the user account was created.
     * <p>Automatically populated via {@link #prePersist()} before persisting.</p>
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp marking the last time the user account was updated.
     * <p>Automatically populated via {@link #preUpdate()} before updating.</p>
     */
    private LocalDateTime updatedAt;

    // -- Roles and Permissions --

    /**
     * The user's assigned role within the system.
     * <p>Defines their access level (e.g., USER)</p>
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * Lifecycle callback executed before persisting the entity.
     * <p>Automatically sets the {@link #createdAt} timestamp to the current time.</p>
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback executed before updating the entity.
     * <p>Automatically sets the {@link #updatedAt} timestamp to the current time.</p>
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
