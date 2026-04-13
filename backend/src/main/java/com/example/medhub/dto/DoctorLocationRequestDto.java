package com.example.medhub.dto;

public record DoctorLocationRequestDto(
        Long id,
        String locationName,
        String city,
        String address) {
}
