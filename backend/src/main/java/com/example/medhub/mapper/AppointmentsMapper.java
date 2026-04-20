package com.example.medhub.mapper;

import com.example.medhub.dto.response.AppointmentsDto;
import com.example.medhub.entity.AppointmentsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { DoctorMapper.class })
public interface AppointmentsMapper {

    @Mapping(source = "patient", target = "user")
    AppointmentsDto toAppointmentDto(AppointmentsEntity appointment);
}
