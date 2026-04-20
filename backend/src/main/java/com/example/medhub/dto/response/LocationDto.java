package com.example.medhub.dto.response;

public record LocationDto(
        Long locationId,
        String locationName,
        String address,
        String city,
        String country,
        String phoneNumber,
        String email,
        String description,
        Integer yearEstablished) {
}
