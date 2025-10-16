package com.ridei.apirest.ridei_apirest.user.controller;

import com.ridei.apirest.ridei_apirest.common.response.ApiResponseDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for mapping user-related operations.
 * <p>
 *     This class exposes endpoints for:
 *     <ul>
 *         <li>Retrieving all registered users</li>
 *         <li>Creating new users</li>
 *     </ul>
 * </p>
 * <p>
 *     It communicates with the {@link UserService} layer to handle business logic
 *     and wraps responses using the standardized {@link ApiResponseDto} structure.
 * </p>
 * <p>
 *     Each response includes a <b>correlation ID</b> extracted from {@link MDC}
 *     (Mapped Diagnostic Context) to help trace logs across the request lifestyle.
 * </p>
 * <p>
 *     Swagger/OpenAPI annotations are used to:
 *     <ul>
 *         <li>Generate API documentation</li>
 *         <li>Provide summaries and detailed descriptions for each endpoint</li>
 *         <li>Document possible HTTP response codes</li>
 *     </ul>
 * </p>
 * <p><b>Base URL: {@code /api/users}</b></p>
 *
 * <p><b>Example requests:</b></p>
 * <pre>
 *     GET /api/users
 *     POST /api/users
 *     Content-Type: application/json
 *     {
 *         "email": "john.doe@example.com",
 *         "firstName": "John",
 *         "lastName": "Doe",
 *         "password": "123456"
 *     }
 * </pre>
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "System user management")
public class UserController {

    /**
     * Service layer dependency used to execute business operations related to users.
     */
    @Autowired
    private UserService userService;

    /**
     * Retrieves a list of all registered users in the system.
     * <p>
     *     This endpoint is typically used by administrators or internal modules
     *     that require access to user data.
     *
     *     <p><b>Workflow:</b></p>
     *     <ol>
     *         <li>Delegate to {@link UserService#findAllUsers()} to fetch data.</li>
     *         <li>Retrieves the current {@code correlationId} from {@link MDC}.</li>
     *         <li>Wraps the result in {@link ApiResponseDto} for standardized API responses.</li>
     *     </ol>
     * </p>
     *
     * @return a {@link ResponseEntity} containing an {@link ApiResponseDto}
     *          with the list of {@link UserResponse} objects.
     *
     * <p><b>Possible responses:</b></p>
     * <ul>
     *     <li><b>200 OK:</b> User successfully obtained</li>
     *     <li><b>500 Internal Server Error:</b> Unexpected server error</li>
     * </ul>
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
     * <p>
     *     The request body must contain a valid {@link UserRequest} object, validated
     *     using {@code @Valid} annotations.
     *     If any validation errors occur, they are handled globally by
     *     {@link com.ridei.apirest.ridei_apirest.common.exception.GlobalExceptionHandler}
     * </p>
     * <p><b>Workflow:</b></p>
     * <ol>
     *     <li>Validates incoming request using Bean Validation annotations.</li>
     *     <li>Delegates creation logic to {@link UserService#createUser(UserRequest)}.</li>
     *     <li>Retrieves the current {@code correlationId} for traceability</li>
     *     <li>Returns a standardized success response with the created user.</li>
     * </ol>
     *
     * @param request the {@link UserRequest} DTO containing user registration data.
     *                Must pass all validation rules.
     * @return a {@link ResponseEntity} containing an {@link ApiResponseDto}
     *         with the created {@link UserResponse} object.
     *
     * <p><b>Possible responses:</b></p>
     * <ul>
     *     <li><b>200 OK:</b> User successfully created</li>
     *     <li><b>400 Bad Request:</b> Validation failed</li>
     *     <li><b>500 Internal Server Error:</b> Unexpected error</li>
     * </ul>
     */
    @PostMapping
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
}
