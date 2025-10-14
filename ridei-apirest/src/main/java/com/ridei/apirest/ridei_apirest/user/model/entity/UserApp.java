package com.ridei.apirest.ridei_apirest.user.model.entity;

import com.ridei.apirest.ridei_apirest.user.model.AuthenticationProvider;
import com.ridei.apirest.ridei_apirest.user.model.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private AuthenticationProvider provider; // "google" o Local

    // --- Dades personals ---
    private String firstName;
    private String lastName;

    // -- Estat i auditoria --
    private boolean enabled = true;
    private boolean emailVerified = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // -- Rols i permisos --
    @Enumerated(EnumType.STRING)
    private Role role;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
