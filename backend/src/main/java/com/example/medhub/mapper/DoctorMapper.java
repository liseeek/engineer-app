package com.example.medhub.mapper;

import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.DoctorDto;
import com.example.medhub.entity.DoctorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    DoctorEntity toDoctor(DoctorCreateRequestDto createRequestDto);

    DoctorDto toDoctorDto(DoctorEntity doctorEntity);

    @Mapping(target = "specialization", source = "specialization")
    DoctorDto toDoctorDto(DoctorEntity doctorEntity, SpecializationDto specialization);
}
