package com.example.medhub.mapper;

import com.example.medhub.dto.response.LocationDto;
import com.example.medhub.dto.request.LocationCreateRequestDto;
import com.example.medhub.entity.LocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toLocationDto(LocationEntity locationEntity);

    @Mapping(target = "locationId", ignore = true)
    @Mapping(target = "workers", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "yearEstablished", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "email", ignore = true)
    LocationEntity toLocationEntity(LocationCreateRequestDto locationCreateRequestDto);
}
