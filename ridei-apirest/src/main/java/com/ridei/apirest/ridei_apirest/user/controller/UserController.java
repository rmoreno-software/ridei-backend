package com.ridei.apirest.ridei_apirest.user.controller;

import com.ridei.apirest.ridei_apirest.common.response.ApiResponseDto;
import com.ridei.apirest.ridei_apirest.security.JwtTokenProvider;
import com.ridei.apirest.ridei_apirest.user.model.dto.LoginRequest;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserRequest;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserResponse;
import com.ridei.apirest.ridei_apirest.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller responsible for managing system users.
 *
 * <p>This controller exposes endpoints to:
 * <ul>
 *     <li>List all registered users</li>
 *     <li>Create a new user in the system</li>
 * </ul>
 *
 * <p>Responses are standardized using {@link ApiResponseDto}, which includes:
 * <ul>
 *     <li>Success status</li>
 *     <li>Data payload</li>
 *     <li>Optional error messages</li>
 *     <li>Correlation ID for request tracing</li>
 * </ul>
 *
 * <p>Swagger/OpenAPI annotations provide documentation and API metadata
 * to support automatic generation of API docs and interactive UI.
 *
 * <p>Correlation IDs are automatically retrieved from the logging context (MDC)
 * to allow tracing requests through logs and services.
 *
 * @see UserService
 * @see ApiResponseDto
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "System user management")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Lists all registered users.
     *
     * <p>This endpoint fetches all users from the service layer,
     * wraps the result in a standardized {@link ApiResponseDto},
     * and returns HTTP 200 with the list of users.
     *
     * @return ResponseEntity containing a list of {@link UserResponse} objects
     *         wrapped in {@link ApiResponseDto}, including correlation ID
     */
    @GetMapping
    @Operation(summary = "List all users", description = "Returns a list of all registered users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users successfully obtained"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ApiResponseDto<List<UserResponse>>> listAllUsers() {
        // Fetch all users via the service layer
        List<UserResponse> users = userService.findAllUsers();

        // Retrieve the correlation ID from the logging context
        String correlationId = MDC.get("correlationId");

        // Return a standardized success response
        return ResponseEntity.ok(ApiResponseDto.success(users, correlationId));
    }

    /**
     * Creates a new user in the system.
     *
     * <p>The request body must contain a valid {@link UserRequest}.
     * Validation errors are automatically handled by global exception handlers.
     * The response includes the created user data, standardized in {@link ApiResponseDto},
     * and a correlation ID for logging and tracing.
     *
     * @param request The user creation request payload
     * @return ResponseEntity containing the created {@link UserResponse}
     *         wrapped in {@link ApiResponseDto}, including correlation ID
     */
    @PostMapping("/auth")
    @Operation(summary = "Create an user", description = "Registers a ser to the system and returns it")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users successfully created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ApiResponseDto<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request ) {

        // Delegate user creation to the service Layer
        UserResponse user = userService.createUser(request);

        // Retrieve correlation ID from the logging context
        String correlationId = MDC.get("correlationId");

        // Return standardized API response with created user details
        return ResponseEntity.ok(ApiResponseDto.success(user, correlationId));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponseDto<?>> login(@Valid @RequestBody LoginRequest body) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.getEmail(), body.getPassword())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtTokenProvider.createToken(userDetails.getUsername(), userDetails.getAuthorities());

            return ResponseEntity.ok(ApiResponseDto.success(
                    Map.of("accessToken", token),
                    MDC.get("correlationId")
            ));
        } catch (BadCredentialsException be) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("Invalid email or password", MDC.get("correlationId")));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("Authentication failed", MDC.get("correlationId")));
        }
    }
}
