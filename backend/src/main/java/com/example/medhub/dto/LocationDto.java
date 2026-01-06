package com.example.medhub.dto;

import com.example.medhub.entity.LocationEntity;
import com.example.medhub.mapper.LocationMapper;

public record LocationDto(Long locationId, String locationName, String address, String city, String country) {
    public static LocationDto from(LocationEntity locationEntity) {
        return LocationMapper.LOCATION_MAPPER.toLocationDto(locationEntity);
    }
}
