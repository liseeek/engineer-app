package com.example.medhub.dto;

import com.example.medhub.entity.LocationEntity;
import com.example.medhub.mapper.LocationMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LocationDto {
    private final Long locationId;
    private final String locationName;
    private final String address;
    private final String city;
    private final String country;

    public static LocationDto from(LocationEntity locationEntity) {
        return LocationMapper.LOCATION_MAPPER.toLocationDto(locationEntity);
    }
}
