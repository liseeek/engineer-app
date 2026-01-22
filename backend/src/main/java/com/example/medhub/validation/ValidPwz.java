package com.example.medhub.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PwzValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPwz {
    String message() default "Invalid PWZ format (must be 7 digits)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
