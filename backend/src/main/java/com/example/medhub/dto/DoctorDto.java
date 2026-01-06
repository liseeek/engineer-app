package com.example.medhub.dto;

import com.example.medhub.entity.DoctorEntity;
import com.example.medhub.mapper.DoctorMapper;

import java.util.List;

public record DoctorDto(Long doctorId, String name, String surname, List<LocationDto> locations,
                        SpecializationDto specialization) {
    public static DoctorDto from(DoctorEntity doctorEntity, SpecializationDto specialization) {
        return DoctorMapper.DOCTOR_MAPPER.toDoctorDto(doctorEntity, specialization);
    }

    public static DoctorDto from(DoctorEntity doctorEntity) {
        return DoctorMapper.DOCTOR_MAPPER.toDoctorDto(doctorEntity);
    }
}