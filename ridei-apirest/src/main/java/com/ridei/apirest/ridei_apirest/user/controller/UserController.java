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

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "System user management")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "List all users", description = "Returns a list of all registered users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users successfully obtained"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ApiResponseDto<List<UserResponse>>> listAllUsers() {
        List<UserResponse> users = userService.findAllUsers();
        String correlationId = MDC.get("correlationId");
        return ResponseEntity.ok(ApiResponseDto.success(users, correlationId));
    }

    @PostMapping
    @Operation(summary = "Create an user", description = "Registers a ser to the system and returns it")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users successfully created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ApiResponseDto<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request ) {
        UserResponse user = userService.createUser(request);
        String correlationId = MDC.get("correlationId");
        return ResponseEntity.ok(ApiResponseDto.success(user, correlationId));
    }
}
