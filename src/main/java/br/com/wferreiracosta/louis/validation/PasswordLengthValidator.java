package br.com.wferreiracosta.louis.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

public class PasswordLengthValidator implements ConstraintValidator<PasswordLength, String> {
    public static final String MESSAGE = "Password must be at most 72 UTF-8 bytes";

    public static boolean withinLimit(String password) {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return withinLimit(value);
    }
}
