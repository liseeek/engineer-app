package com.example.medhub.validation;

import com.example.medhub.repository.DoctorRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniquePwzValidator implements ConstraintValidator<UniquePwz, String> {

    private final DoctorRepository doctorRepository;

    @Override
    public boolean isValid(String pwz, ConstraintValidatorContext context) {
        if (pwz == null || pwz.isBlank()) {
            return true;
        }

        return !doctorRepository.existsByPwz(pwz);
    }
}
