package com.example.medhub.dto.response;

import java.util.List;

public record DoctorDto(
        Long doctorId,
        String name,
        String surname,
        String email,
        String pwz,
        String bio,
        String avatarUrl,
        String verificationStatus,
        List<LocationDto> locations,
        List<SpecializationDto> specializations) {
}
