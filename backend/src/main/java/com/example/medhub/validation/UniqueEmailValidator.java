package com.example.medhub.validation;

import org.springframework.stereotype.Component;

import com.example.medhub.repository.UserRepository;
import com.example.medhub.repository.WorkerRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;

    public UniqueEmailValidator(UserRepository userRepository, WorkerRepository workerRepository) {
        this.userRepository = userRepository;
        this.workerRepository = workerRepository;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true;
        }

        return !userRepository.existsByEmail(email) && !workerRepository.existsByEmail(email);

    }
}