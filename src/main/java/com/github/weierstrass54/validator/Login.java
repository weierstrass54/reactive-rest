package com.github.weierstrass54.validator;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

@Constraint(validatedBy = Login.LoginValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Login {
    String message() default "Логин не должен содержать пробелов и быть длиной от 3 до 50 символов.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    boolean nullable() default false;

    class LoginValidator implements ConstraintValidator<Login, String> {
        private boolean nullable;

        @Override
        public void initialize(Login constraintAnnotation) {
            nullable = constraintAnnotation.nullable();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return Optional.ofNullable(value)
                .map(v -> v.length() >=3 && v.length() <= 50 && v.indexOf(' ') == -1)
                .orElse(nullable);
        }
    }
}
