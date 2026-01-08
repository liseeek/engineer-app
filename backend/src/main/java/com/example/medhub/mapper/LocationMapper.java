package com.example.medhub.mapper;

import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.LocationCreateRequestDto;
import com.example.medhub.entity.LocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class LocationMapper {
    public static final LocationMapper LOCATION_MAPPER = Mappers.getMapper(LocationMapper.class);

    public abstract LocationDto toLocationDto(LocationEntity locationEntity);

    @Mapping(target = "locationId", ignore = true)
    @Mapping(target = "workers", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    public abstract LocationEntity toLocationEntity(LocationCreateRequestDto locationCreateRequestDto);
}
