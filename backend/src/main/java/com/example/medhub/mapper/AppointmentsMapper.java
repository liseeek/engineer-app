package com.example.medhub.mapper;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.entity.AppointmentsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { DoctorMapper.class, UserMapper.class })
public interface AppointmentsMapper {

    @Mapping(source = "patient", target = "user")
    AppointmentsDto toAppointmentDto(AppointmentsEntity appointment);
}
