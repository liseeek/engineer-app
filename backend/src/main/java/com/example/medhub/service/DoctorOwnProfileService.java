package com.example.medhub.service;

import com.example.medhub.dto.request.DoctorOwnProfileUpdateRequestDto;
import com.example.medhub.dto.response.DoctorDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.mapper.DoctorMapper;
import com.example.medhub.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorOwnProfileService {

    private final SecurityService securityService;
    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    @Transactional
    public DoctorDto updateOwnProfile(DoctorOwnProfileUpdateRequestDto dto) {
        Doctor doctor = securityService.getCurrentDoctor();
        if (dto.getBio() != null) {
            doctor.setBio(dto.getBio());
        }
        if (dto.getAvatarUrl() != null) {
            doctor.setAvatarUrl(dto.getAvatarUrl());
        }
        return doctorMapper.toDoctorDto(doctorRepository.save(doctor));
    }

    public DoctorDto getOwnProfile() {
        Doctor doctor = securityService.getCurrentDoctor();
        return doctorMapper.toDoctorDto(doctor);
    }
}
