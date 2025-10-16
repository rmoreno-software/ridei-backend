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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Dades d'autenticación ---
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    // --- Dades de Google Auth ---
    private String googleId;  // ID únic proporcionat per google
    private String pictureUrl; // URL del avatar de Google

    @Enumerated(EnumType.STRING)
    private AuthenticationProvider provider; // "google" o Local

    // --- Dades personals ---
    private String firstName;
    private String lastName;

    // -- Estat i auditoria --
    private boolean enabled = true;
    private boolean emailVerified = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // -- Rols i permisos --
    @Enumerated(EnumType.STRING)
    private Role role;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
