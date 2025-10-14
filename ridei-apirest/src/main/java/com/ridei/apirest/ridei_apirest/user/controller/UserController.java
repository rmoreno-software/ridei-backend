package com.ridei.apirest.ridei_apirest.user.controller;

import com.ridei.apirest.ridei_apirest.common.response.ApiResponse;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserRequest;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserResponse;
import com.ridei.apirest.ridei_apirest.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listAllUsers() {
        List<UserResponse> users = userService.findAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request ) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
