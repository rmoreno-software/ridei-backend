package com.ridei.identity.domain.port.out;

import java.util.Optional;

import com.ridei.identity.domain.model.User;

/**
 * Output Port (Driven Port) interface for User persistence operations.
 * <p>
 * This interface defines the contract for storing and retrieving {@link User} aggregate roots.
 * By placing this interface in the Domain layer, we decouple the business logic from
 * specific persistence technologies (e.g., JPA, Hibernate, JDBC).
 * </p>
 * <p>
 * <b>Mapping Note:</b> Implementations of this interface (Adapters) are responsible for
 * mapping between the Domain Entity ({@link User}) and the Infrastructure Entity (e.g., JPA Entity).
 * </p>
 */
public interface UserRepository {
    
    /**
     * Persists the state of the provided User aggregate root.
     * <p>
     * This method handles both the creation of new records and the update of existing ones,
     * abstracting the underlying "INSERT" or "UPDATE" mechanics.
     * </p>
     *
     * @param user The domain user entity to be saved. Must not be {@code null}.
     */
    void save(User user);

    /**
     * Retrieves a User entity based on their unique email address.
     * <p>
     * This method is primarily used for business rules validation (e.g., uniqueness checks)
     * and authentication processes.
     * </p>
     *
     * @param email The email address to search for.
     * @return An {@link Optional} containing the found {@link User}, or {@code Optional.empty()}
     * if no user is found with the provided email. This forces explicit handling of
     * the "not found" scenario by the caller.
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves a User entity based on their unique nickname.
     * <p>
     * This method is primarily used for business rules validation (e.g., uniqueness checks)
     * and authentication processes.
     * </p>
     *
     * @param nickname The nickname to search for.
     * @return An {@link Optional} containing the found {@link User}, or {@code Optional.empty()}
     * if no user is found with the provided nickname. This forces explicit handling of
     * the "not found" scenario by the caller.
     */
    Optional<User> findByNickname(String nickname);
}
