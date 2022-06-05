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
import java.util.regex.Pattern;

@Constraint(validatedBy = Password.PasswordValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "Пароль должен иметь длину от 6 до 50 символов, содержать хотя бы одну букву и цифру и не содержать пробелов.";
    boolean nullable() default false;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class PasswordValidator implements ConstraintValidator<Password, String> {
        private static final Pattern DIGIT_REGEX = Pattern.compile("\\d");
        private static final Pattern DIGITS_ONLY_REGEX = Pattern.compile("^\\d+$");

        private boolean nullable;

        @Override
        public void initialize(Password constraintAnnotation) {
            nullable = constraintAnnotation.nullable();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return Optional.ofNullable(value).map(this::isCorrect).orElse(nullable);
        }

        private boolean isCorrect(String value) {
            if (value.length() < 6 || value.length() > 50) {
                return false;
            }
            if (value.indexOf(' ') != -1) {
                return false;
            }
            if (DIGITS_ONLY_REGEX.matcher(value).matches()) {
                return false;
            }
            return DIGIT_REGEX.matcher(value).find();
        }
    }

}
