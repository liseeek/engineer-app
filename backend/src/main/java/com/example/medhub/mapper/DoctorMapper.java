package com.example.medhub.mapper;

import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.DoctorDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.SpecializationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = LocationMapper.class)
public interface DoctorMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "specializations", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "authority", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    Doctor toDoctor(DoctorCreateRequestDto createRequestDto);

    @Mapping(source = "userId", target = "doctorId")
    @Mapping(target = "specializations", expression = "java(mapSpecializationDtos(doctorEntity))")
    DoctorDto toDoctorDto(Doctor doctorEntity);

    default List<SpecializationDto> mapSpecializationDtos(Doctor doctor) {
        if (doctor.getSpecializations() == null) {
            return List.of();
        }
        return doctor.getSpecializations().stream().map(SpecializationDto::from).toList();
    }

    default List<SpecializationDto> mapSpecializationEntities(List<SpecializationEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(SpecializationDto::from).toList();
    }
}
