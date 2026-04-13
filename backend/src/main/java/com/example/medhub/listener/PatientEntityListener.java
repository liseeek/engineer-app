package com.example.medhub.listener;

import com.example.medhub.entity.Patient;
import com.example.medhub.service.CryptoService;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatientEntityListener {
    private final CryptoService cryptoService;

    @PrePersist
    @PreUpdate
    public void setPeselHash(Patient patient) {
        String pesel = patient.getPesel();
        if (pesel != null) {
            String hashedPesel = cryptoService.hash(pesel);
            patient.setPeselHash(hashedPesel);
        }
    }
}
