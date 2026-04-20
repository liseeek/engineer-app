package com.example.medhub.dto.response;

public record DoctorLocationRequestDto(
        Long id,
        String locationName,
        String city,
        String address) {
}
