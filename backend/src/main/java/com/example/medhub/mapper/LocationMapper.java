package com.example.medhub.mapper;

import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.LocationCreateRequestDto;
import com.example.medhub.entity.LocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toLocationDto(LocationEntity locationEntity);

    @Mapping(target = "locationId", ignore = true)
    @Mapping(target = "workers", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    LocationEntity toLocationEntity(LocationCreateRequestDto locationCreateRequestDto);
}
