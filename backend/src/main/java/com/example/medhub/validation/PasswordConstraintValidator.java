package com.example.medhub.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                // Length Rule (8-128 characters)
                new LengthRule(8, 128),

                // At least one uppercase letter
                new CharacterRule(EnglishCharacterData.UpperCase, 1),

                // At least one number
                new CharacterRule(EnglishCharacterData.Digit, 1),

                // At least one special character from the frontend set: @#$%^&+=!
                new CharacterRule(new CharacterData() {
                    @Override
                    public String getErrorCode() {
                        return "INSUFFICIENT_SPECIAL";
                    }

                    @Override
                    public String getCharacters() {
                        return "@#$%^&+=!";
                    }
                }, 1),

                // No whitespace
                new WhitespaceRule()
        ));

        RuleResult result = validator.validate(new PasswordData(password));

        if (result.isValid()) {
            return true;
        }

        // Customizing error messages to be user-friendly
        List<String> messages = validator.getMessages(result);
        String messageTemplate = messages.stream().collect(Collectors.joining(", "));
        
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();

        return false;
    }
}
