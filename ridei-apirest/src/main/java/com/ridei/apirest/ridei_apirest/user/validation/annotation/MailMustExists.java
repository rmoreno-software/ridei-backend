package com.ridei.apirest.ridei_apirest.user.validation.annotation;

import com.ridei.apirest.ridei_apirest.user.validation.validator.MailMustExistsValidator;
import com.ridei.apirest.ridei_apirest.user.validation.validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MailMustExistsValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MailMustExists {

    String message() default "{login.user.notfound}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
