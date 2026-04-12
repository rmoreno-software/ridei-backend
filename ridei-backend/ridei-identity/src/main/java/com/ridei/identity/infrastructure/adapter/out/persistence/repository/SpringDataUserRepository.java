package com.ridei.identity.infrastructure.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ridei.identity.infrastructure.adapter.out.persistence.entity.UserEntity;

/**
 * Spring Data JPA specific repository interface for {@link UserEntity}.
 * <p>
 * This interface acts as a <b>helper</b> for the Persistence Adapter implementation.
 * It extends {@link JpaRepository} to inherit standard CRUD operations (save, findById, delete)
 * and pagination support without the need for boilerplate code.
 * </p>
 * <p>
 * <b>Architectural Boundary:</b> This interface belongs strictly to the Infrastructure layer.
 * It deals with {@link UserEntity} (Database Table representation), not the Domain Model.
 * The Domain Service should <b>never</b> inject this interface directly; it must go through
 * the {@link com.ridei.identity.application.port.out.UserRepository} port.
 * </p>
 */
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
    
    /**
     * Derived Query Method to find a user by their email address.
     * <p>
     * <b>Spring Magic:</b> Spring Data automatically generates the SQL query at runtime
     * based on the method name convention (e.g., {@code SELECT * FROM users WHERE email = ?}).
     * </p>
     *
     * @param email The exact email to search for.
     * @return An {@link Optional} containing the <b>Infrastructure Entity</b> if found.
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Derived Query Method to find a user by their nickname.
     * <p>
     * <b>Spring Magic:</b> Spring Data automatically generates the SQL query at runtime
     * based on the method name convention (e.g., {@code SELECT * FROM users WHERE nickname = ?}).
     * </p>
     *
     * @param nickname The exact nickname to search for.
     * @return An {@link Optional} containing the <b>Infrastructure Entity</b> if found.
     */
    Optional<UserEntity> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);
}
