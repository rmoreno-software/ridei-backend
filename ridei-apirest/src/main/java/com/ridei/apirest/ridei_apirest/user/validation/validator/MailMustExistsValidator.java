package com.ridei.apirest.ridei_apirest.user.validation.validator;

import com.ridei.apirest.ridei_apirest.user.service.UserService;
import com.ridei.apirest.ridei_apirest.user.validation.annotation.MailMustExists;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class MailMustExistsValidator implements ConstraintValidator<MailMustExists, String> {

    @Autowired
    private UserService userService;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return email == null || userService.isEmailTaken(email);
    }

}
