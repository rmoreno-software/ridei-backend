package com.ridei.apirest.ridei_apirest.user.model;

/**
 * Enumeration that defines the different user roles available
 * in the application.
 *
 * <p>Each role determines the level of access and permissions a user
 * has within the system. This enum is typically used in combination
 * with Spring Security or custom access control logistic to authorize
 * user actions.</p>
 *
 * <p><b>Possible values:</b></p>
 * <ul>
 *     <li>{@link #USER} - Standard application user with limited access.</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 *     // Assign a role to a user
 *     user.setRole(Role.USER);
 *
 *     // Check if user has admin privileges
 *     if (user.getRole() == ROLE.ADMIN) {
 *         // perform actions
 *     }
 * </pre>
 *
 * <p>This enum is stored in the database as a string representation
 * when user in JPA entities (via {@code @Enumerated(EnumType.STRING)}).</p>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
public enum Role {

    /**
     * Regular user role with standard permissions.
     */
    USER
}
