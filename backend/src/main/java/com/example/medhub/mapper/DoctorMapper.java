package com.example.medhub.mapper;

import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.DoctorDto;
import com.example.medhub.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { LocationMapper.class, SpecializationMapper.class })
public interface DoctorMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "authority", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    Doctor toDoctor(DoctorCreateRequestDto createRequestDto);

    @Mapping(source = "userId", target = "doctorId")
    DoctorDto toDoctorDto(Doctor doctorEntity);

    @Mapping(target = "specialization", source = "specialization")
    @Mapping(source = "doctorEntity.userId", target = "doctorId")
    DoctorDto toDoctorDto(Doctor doctorEntity, SpecializationDto specialization);
}
