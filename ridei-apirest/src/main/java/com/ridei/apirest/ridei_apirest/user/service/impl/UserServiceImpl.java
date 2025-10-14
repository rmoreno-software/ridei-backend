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

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        UserApp user = userMapper.toEntity(request);

        UserApp saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }
}
