package com.ridei.apirest.ridei_apirest.user.service.impl;

import com.ridei.apirest.ridei_apirest.user.model.dto.UserRequest;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserResponse;
import com.ridei.apirest.ridei_apirest.user.model.entity.UserApp;
import com.ridei.apirest.ridei_apirest.user.model.mapper.UserMapper;
import com.ridei.apirest.ridei_apirest.user.repository.UserRepository;
import com.ridei.apirest.ridei_apirest.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link UserService} interface responsible for handling
 * all user-related business logic in the application.
 *
 * <p>This service acts as an intermediary layer between the controller and the
 * persistence layer, managing user creation, retrieval, and validation
 * operations. It ensures that entity-to-DTO conversions are handled
 * consistently through the {@link UserMapper} component.</p>
 *
 * <p><b>Main Responsibilities:</b></p>
 * <ul>
 *     <li>Retrieve and map all users from the database.</li>
 *     <li>Check if a specific email address is already registered.</li>
 *     <li>Create new users based on incoming requests.</li>
 * </ul>
 *
 * <p>All returned DTOs are designed to hide sensitive data (e.g., password)
 * and provide only the information required by the API client.</p>
 *
 * <p>This class is annotated with {@link Service}, allowing it to be automatically
 * discovered and injected by Spring's dependency injection mechanism.</p>
 *
 * @see com.ridei.apirest.ridei_apirest.user.controller.UserController
 * @see UserMapper
 * @see UserRepository
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Service
public class UserServiceImpl implements UserService {

    /**
     * Repository for performing CRUD operations on {@link UserApp} entities.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Mapper used to convert between {@link UserApp} entities and DTOs ({@link UserRequest}, {@link UserResponse}).
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Retrieves all users from the database and converts them into {@link UserResponse} DTOs.
     * 
     * <p>This method uses a stream to map each {@link UserApp} entity to a corresponding
     * DTO through the {@link UserMapper}. It is typically used by the controller
     * to return a list of users to the client.</p>
     * 
     * @return a list of {@link UserResponse} objects representing all registered users
     */
    @Override
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Checks whether a user with the given email address already exists in the system.
     * 
     * <p>This method delegates to the repository's {@link UserRepository#existsByEmail(String)}
     * to perform a database query, ensuring that duplicate emails are not allowed
     * during user registration.</p>
     * 
     * @param email the email address to check (must not be {@code null})
     * @return {@code true} if the email is already taken, otherwise {@code false}
     */
    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Creates a new user based on the provided {@link UserRequest} data.
     * 
     * <p>This method performs the following steps:</p>
     * <ol>
     *     <li>Maps the incoming request DTO to a {@link UserApp} entity using {@link UserMapper#toEntity(UserRequest)}.</li>
     *     <li>Saves the entity to the database using {@link UserRepository#save(Object)}.</li>
     *     <li>Maps the saved entity back to a {@link UserResponse} DTO to return to the client.</li>
     * </ol>
     *
     * <p>All password encryption or additional validation logic can be added here
     * before saving the user.</p>
     *
     * @param request the {@link UserRequest} DTO containing user registration data
     * @return a {@link UserResponse} DTO representing the newly created user
     */
    @Override
    public UserResponse createUser(UserRequest request) {
        UserApp user = userMapper.toEntity(request);
        UserApp saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }
}
