package com.example.medhub.mapper;

import com.example.medhub.dto.response.SpecializationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.response.DoctorDto;
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
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    Doctor toDoctor(DoctorCreateRequestDto createRequestDto);

    @Mapping(source = "userId", target = "doctorId")
    @Mapping(target = "specializations", expression = "java(mapSpecializationDtos(doctorEntity))")
    DoctorDto toDoctorDto(Doctor doctorEntity);

    default List<SpecializationDto> mapSpecializationDtos(Doctor doctor) {
        if (doctor.getSpecializations() == null) {
            return List.of();
        }
        return mapSpecializationEntities(doctor.getSpecializations());
    }

    default List<SpecializationDto> mapSpecializationEntities(List<SpecializationEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(entity -> new SpecializationDto(entity.getSpecializationId(), entity.getSpecializationName()))
                .toList();
    }
}
