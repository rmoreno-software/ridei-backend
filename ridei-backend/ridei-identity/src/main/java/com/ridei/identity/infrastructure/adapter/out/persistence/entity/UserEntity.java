package com.ridei.identity.infrastructure.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Infrastructure Entity representing the persistent state of a User in the database.
 * <p>
 * This class maps the internal Domain Model ({@link com.ridei.identity.domain.model.User})
 * to the relational database table {@code users} using Jakarta Persistence API (JPA).
 * </p>
 * <p>
 * <b>Architectural Note:</b> This is an "Anemic Domain Model" by design. It contains
 * only data and getters/setters required by the ORM (Hibernate). Business logic
 * strictly belongs to the Domain layer, not here.
 * </p>
 */
@Entity
@Table(name = "users") // Maps to the 'users' SQL table
@Getter
@Setter
@NoArgsConstructor // CRITICAL: Required by Hibernate to instantiate the class via Reflection
@AllArgsConstructor // Convenience for the Mapper
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    
    /**
     * The Primary Key.
     * <p>
     * Note: We do not use {@code @GeneratedValue} here because the UUID is assigned
     * by the Domain Layer (Aggregate Root) before persistence, ensuring the ID is known
     * immediately upon object creation.
     * </p>
     */
    @Id
    private UUID id;

    /**
     * The user's email address.
     * <p>
     * Indexed unique column to prevent duplicate registrations at the database level.
     * </p>
     */
    @Column(nullable = false, unique = true) // Enforce DB constraints
    private String email;

    /**
     * The user's nickname.
     * <p>
     * Indexed unique column to prevent duplicate registrations at the database level.
     * </p>
     */
    @Column(nullable = false, unique = true) // Enforce DB constraints
    private String nickname;

    /**
     * The BCrypt hashed password.
     */
    @Column(nullable = false)
    private String password;

    /**
     * The user's full name.
     */
    @Column(nullable = false)
    private String name;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
