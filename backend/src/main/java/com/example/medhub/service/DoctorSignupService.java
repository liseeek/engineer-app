package com.example.medhub.service;

import com.example.medhub.dto.request.DoctorSignupRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.enums.Authority;
import com.example.medhub.enums.DoctorVerificationStatus;
import com.example.medhub.configuration.MedHubProperties;
import com.example.medhub.exceptions.UnauthorizedOperationException;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.SpecializationRepository;
import com.example.medhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorSignupService {

    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MedHubProperties medHubProperties;

    @Transactional
    public void signupDoctor(DoctorSignupRequestDto request) {
        if (!medHubProperties.getDoctorSelfSignup().isEnabled()) {
            throw new UnauthorizedOperationException(
                    "Doctor self-registration is disabled. Enable medhub.doctor-self-signup (e.g. MEDHUB_DOCTOR_SELF_SIGNUP_ENABLED=true) for local or demo use.");
        }
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        if (doctorRepository.existsByPwz(request.getPwz())) {
            throw new IllegalArgumentException("Doctor with this PWZ already exists");
        }

        List<SpecializationEntity> specializations = new ArrayList<>();
        for (Long specId : request.getSpecializationIds()) {
            SpecializationEntity spec = specializationRepository.findById(specId)
                    .orElseThrow(() -> new IllegalArgumentException("Specialization not found: " + specId));
            specializations.add(spec);
        }

        Doctor doctor = new Doctor();
        doctor.setEmail(request.getEmail());
        doctor.setPassword(passwordEncoder.encode(request.getPassword()));
        doctor.setName(request.getName());
        doctor.setSurname(request.getSurname());
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setPwz(request.getPwz());
        doctor.setSpecializations(specializations);
        doctor.setAuthority(Authority.ROLE_DOCTOR);
        doctor.setVerificationStatus(DoctorVerificationStatus.VERIFIED);

        doctorRepository.save(doctor);
        log.info("Doctor self-registered with VERIFIED status: email={}, pwz={}", request.getEmail(), request.getPwz());
    }
}
