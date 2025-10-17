package com.ridei.apirest.ridei_apirest.user.model;

/**
 * Enumeration that defines the available authentication providers
 * supported by the application.
 *
 * <p>This enum is used to identify how a user was authenticated
 * in the system - either through a third-party provider (like Google)
 * or by using the application's local authentication mechanism.</p>
 *
 * <p><b>Possible values:</b></p>
 * <ul>
 *     <li>{@link #GOOGLE} - Indicates that the user authenticated using Google OAuth.</li>
 *     <li>{@link #LOCAL} - Indicates that the user authenticated using local credentials (email and password).</li>
 * </ul>
 *
 * <p>This enum is commonly used within the {@code UserApp} entity
 * to track the origin of user authentication and determine
 * the applicable authentication logic.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 *     // Set the provider when creating a new user
 *     user.setProvider(AuthenticationProvider.LOCAL);
 *
 *     //Check the authentication source
 *     if (user.getProvider() == AuthenticationProvider.GOOGLE) {
 *         // Handle Google-authenticated user
 *
 *     }
 * </pre>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
public enum AuthenticationProvider {
    /**
     * Authentication through Google OAuth provider.
     */
    GOOGLE,

    /**
     * Authentication through the application's local system
     * (using email and password).
     */
    LOCAL
}
