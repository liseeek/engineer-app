package com.example.medhub.mapper;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.entity.AppointmentsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AppointmentsMapper {

    @Mapping(target = "doctor.specialization", source = "doctor.specialization")
    AppointmentsDto toAppointmentDto(AppointmentsEntity appointment);
}
