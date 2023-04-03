package ru.netology.cloudservice.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameValidator implements ConstraintValidator<ValidFileName, String> {
    private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[/\\\\:*?\"<>|]+");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (value == null || value.isBlank()) {
            context.buildConstraintViolationWithTemplate("{filename.empty}")
                    .addConstraintViolation();
            return false;
        }

        Matcher forbiddenCharsMatcher = FORBIDDEN_CHARS_PATTERN.matcher(value);
        if (forbiddenCharsMatcher.find()) {
            context.buildConstraintViolationWithTemplate("{filename.forbidden-chars}")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

}
