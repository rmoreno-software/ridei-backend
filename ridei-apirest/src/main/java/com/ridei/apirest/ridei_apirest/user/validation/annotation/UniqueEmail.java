package com.ridei.apirest.ridei_apirest.user.validation.annotation;

import com.ridei.apirest.ridei_apirest.user.validation.validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "{user.email.exists}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
