package com.ridei.apirest.ridei_apirest.user.validation.validator;

import com.ridei.apirest.ridei_apirest.user.repository.UserRepository;
import com.ridei.apirest.ridei_apirest.user.validation.annotation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return email == null || !userRepository.existsByEmail(email);
    }
}
