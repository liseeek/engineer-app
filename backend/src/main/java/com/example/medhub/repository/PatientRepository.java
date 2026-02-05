package com.example.medhub.repository;

import com.example.medhub.entity.Patient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends BaseUserRepository<Patient> {
    Optional<Patient> findByPesel(String pesel);

    Optional<Patient> findByPeselHash(String peselHash);
}
