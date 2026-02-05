package com.example.medhub.validation;

import com.example.medhub.repository.PatientRepository;
import com.example.medhub.service.CryptoService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniquePeselValidator implements ConstraintValidator<UniquePesel, String> {

    private final PatientRepository patientRepository;
    private final CryptoService cryptoService;

    @Override
    public boolean isValid(String pesel, ConstraintValidatorContext context) {
        if (pesel == null || pesel.isEmpty()) {
            return true;
        }
        String hash = cryptoService.hash(pesel);
        return patientRepository.findByPeselHash(hash).isEmpty();
    }
}
