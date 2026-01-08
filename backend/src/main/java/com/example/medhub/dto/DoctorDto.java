package com.example.medhub.dto;

import java.util.List;

public record DoctorDto(Long doctorId, String name, String surname, List<LocationDto> locations,
                        SpecializationDto specialization) {
}