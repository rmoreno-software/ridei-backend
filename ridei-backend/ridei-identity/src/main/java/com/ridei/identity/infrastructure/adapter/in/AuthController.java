package com.ridei.identity.infrastructure.adapter.in;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.identity.application.port.in.RegisterUserUseCase;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        registerUserUseCase.register(request.email, request.password, request.name);
        return "Successfully registered user";
    }
    

    // Clase DTO interna para recibir el JSON
    public static class RegisterRequest {
        public String email;
        public String password;
        public String name;
    }
}
