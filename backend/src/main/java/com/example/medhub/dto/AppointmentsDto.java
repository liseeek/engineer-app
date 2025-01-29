package com.example.medhub.dto;

import com.example.medhub.entity.AppointmentStatus;
import com.example.medhub.entity.AppointmentType;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.mapper.AppointmentsMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@RequiredArgsConstructor
public class AppointmentsDto {
    private final Long appointmentId;
    private final UserDto user;
    private final DoctorDto doctor;
    private final LocalDate date;
    private final LocalTime time;
    private final LocationDto location;
    private final AppointmentStatus appointmentStatus;
    private final AppointmentType appointmentType;

    public static AppointmentsDto from(AppointmentsEntity appointment) {
        return AppointmentsMapper.APPOINTMENTS_MAPPER.toAppointmentDto(appointment);
    }
}
