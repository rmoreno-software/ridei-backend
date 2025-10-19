package com.ridei.apirest.ridei_apirest.user.repository;

import com.ridei.apirest.ridei_apirest.user.model.entity.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link UserApp} entities.
 *
 * <p>This interface extends {@link JpaRepository}, which provides
 * a full set of CRUD operations, pagination, and sorting capabilities
 * for working with the <code>users</code> table in the database</p>
 *
 * <p>It serves as the main data access layer for user-related operations
 * and can be extended with custom query methods following
 * Spring Data JPA conventions.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li>Automatically implements standard database operations (save, find, delete,etc.).</li>
 *     <li>Allows the creation of custom queries using methods naming conventions.</li>
 *     <li>Integrated with Spring's dependency injection and transaction management.</li>
 * </ul>
 *
 * <p><b>Exemple Usage:</b></p>
 * <pre>{@code
 *      @Service
 *      public class UserService {
 *
 *          @Autowired
 *          private UserRepository userRepository;
 *
 *          public boolean isEmailTaken(String email) {
 *              return userRepository.existsByEmail(email);
 *          }
 *      }
 * }
 * </pre>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Repository
public interface UserRepository extends JpaRepository<UserApp, Long> {

    /**
     * Checks if a user already exists in the system with the specified email address.
     *
     * <p>This method is automatically implemented by Spring Data JPA
     * based on the method name. It generates an SQL query similar to:</p>
     *
     * <pre>
     *     SELECT COUNT(*) > 0 FROM users WHERE email = ?;
     * </pre>
     *
     * @param email the email address to check for existence (must not be {@code null})
     * @return {@code true} if a user with the given email already exists, otherwise {@code false}
     */
    boolean existsByEmail(String email);

    Optional<UserApp> findByEmail(String email);
}
