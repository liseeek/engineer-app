package com.example.medhub.dto.response;

import java.util.List;

public record DoctorDto(
        Long doctorId,
        String name,
        String surname,
        String bio,
        String avatarUrl,
        List<LocationDto> locations,
        List<SpecializationDto> specializations) {
}
