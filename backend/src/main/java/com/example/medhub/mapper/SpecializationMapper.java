package com.example.medhub.mapper;

import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.SpecializationCreateRequestDto;
import com.example.medhub.entity.SpecializationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SpecializationMapper {

    SpecializationDto entityToDto(SpecializationEntity specializationEntity);

    @Mapping(target = "specializationId", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    SpecializationEntity toEntity(SpecializationCreateRequestDto dto);
}
