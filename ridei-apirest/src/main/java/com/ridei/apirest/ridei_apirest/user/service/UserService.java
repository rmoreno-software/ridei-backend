package com.ridei.apirest.ridei_apirest.user.service;

import com.ridei.apirest.ridei_apirest.user.model.dto.UserRequest;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> findAllUsers();
    boolean existsByEmail(String email);
    UserResponse createUser(UserRequest request);
}
