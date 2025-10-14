package com.ridei.apirest.ridei_apirest.user.controller;

import com.ridei.apirest.ridei_apirest.common.response.ApiResponse;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserResponse;
import com.ridei.apirest.ridei_apirest.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
