package com.example.medhub.service.strategy;

import com.example.medhub.dto.request.InvitationRegistrationRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.Invitation;
import com.example.medhub.entity.User;
import com.example.medhub.enums.Authority;
import com.example.medhub.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DoctorRegistrationStrategy implements UserRegistrationStrategy {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(String role) {
        return "DOCTOR".equalsIgnoreCase(role);
    }

    @Override
    public User register(InvitationRegistrationRequestDto request, Invitation invitation) {
        Doctor doctor = new Doctor();

        doctor.setEmail(invitation.getEmail());
        doctor.setName(request.getName());
        doctor.setSurname(request.getSurname());
        doctor.setPassword(passwordEncoder.encode(request.getPassword()));
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setAuthority(Authority.ROLE_DOCTOR);
        doctor.setPwz(invitation.getPwz());
        doctor.setSpecialization(invitation.getSpecialization());

        return doctorRepository.save(doctor);
    }
}
