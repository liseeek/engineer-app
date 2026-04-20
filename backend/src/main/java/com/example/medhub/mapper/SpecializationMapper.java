package com.example.medhub.mapper;

import com.example.medhub.dto.response.SpecializationDto;
import com.example.medhub.dto.request.SpecializationCreateRequestDto;
import com.example.medhub.entity.SpecializationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpecializationMapper {

    SpecializationDto entityToDto(SpecializationEntity specializationEntity);

    @Mapping(target = "specializationId", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    SpecializationEntity toEntity(SpecializationCreateRequestDto dto);
}
