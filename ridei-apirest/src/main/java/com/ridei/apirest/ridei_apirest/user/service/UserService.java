package com.ridei.apirest.ridei_apirest.user.service;

import com.ridei.apirest.ridei_apirest.user.model.dto.UserRequest;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserResponse;

import java.util.List;

/**
 * Service interface that defines the business operations related to user management.
 *
 * <p>This interface provides an abstraction layer between the controller and the
 * data access layer, ensuring a clean separation of concerns. It declares the
 * core methods for handling user data, such as retrieving all users, checking
 * for duplicate emails, and creating new users.</p>
 *
 * <p>All implementations of this interface (e.g., {@code UserServiceImpl})
 * should encapsulate the business logic and ensure that the domain model
 * remains consistent and secure. Password encryption, validation, and mapping
 * to DTOs should be handled appropriately within the implementation layer.</p>
 *
 * <p><b>Main responsibilities:</b></p>
 * <ul>
 *     <li>Provide a clean API for user-related operations.</li>
 *     <li>Abstract the persistence layer from the controllers.</li>
 *     <li>Facilitate unit testing through dependency injection and mocking.</li>
 * </ul>
 *
 * @see com.ridei.apirest.ridei_apirest.user.service.impl.UserServiceImpl
 * @see com.ridei.apirest.ridei_apirest.user.controller.UserController
 * @see com.ridei.apirest.ridei_apirest.user.repository.UserRepository
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
public interface UserService {

    /**
     * Retrieves a list of all registered users in the system.
     *
     * <p>The returned list consists of {@link UserResponse} DTOs that
     * contain only non-sensitive information suitable for API clients.</p>
     *
     * @return a list of {@link UserResponse} objects representing all users.
     */
    List<UserResponse> findAllUsers();

    /**
     * Checks whether a given email address is already in use by another user.
     *
     * <p>This method is typically called before user creation to prevent
     * duplicate registrations.</p>
     *
     * @param email the email address to verify (must not be {@code null})
     * @return {@code true} if the email is already taken, otherwise {@code false}
     */
    boolean isEmailTaken(String email);

    /**
     * Creates a new user in the system using the provided registration data.
     *
     * <p>The input {@link UserRequest} DTO contains the necessary fields
     * such as email, name, and password. Implementations should handle
     * password hashing, validation, and default role assignment before
     * persisting the user.</p>
     *
     * @param request the {@link UserRequest} object containing user details
     * @return a {@link UserResponse} DTO representing the newly created user
     */
    UserResponse createUser(UserRequest request);
}
