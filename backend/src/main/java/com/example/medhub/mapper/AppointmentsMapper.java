package com.example.medhub.mapper;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.entity.AppointmentsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class AppointmentsMapper {
    public static final AppointmentsMapper APPOINTMENTS_MAPPER = Mappers.getMapper(AppointmentsMapper.class);

    @Mapping(target = "doctor.specialization", source = "doctor.specialization")
    public abstract AppointmentsDto toAppointmentDto(AppointmentsEntity appointment);
}
