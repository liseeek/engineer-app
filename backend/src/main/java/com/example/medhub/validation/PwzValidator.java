package com.example.medhub.validation;

import com.example.medhub.repository.DoctorRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class PwzValidator implements ConstraintValidator<ValidPwz, String> {

    private final DoctorRepository doctorRepository;

    public PwzValidator(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public boolean isValid(String pwz, ConstraintValidatorContext context) {
        if (pwz == null || pwz.isBlank()) {
            return true;
        }

        if (!pwz.matches("^\\d{7}$")) {
            setCustomMessage(context, "Invalid PWZ format (must be 7 digits)");
            return false;
        }

        if (doctorRepository.existsByPwz(pwz)) {
            setCustomMessage(context, "PWZ already exists");
            return false;
        }

        return true;
    }

    private void setCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
