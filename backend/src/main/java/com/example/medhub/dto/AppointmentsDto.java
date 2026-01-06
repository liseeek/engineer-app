package com.example.medhub.dto;

import com.example.medhub.entity.AppointmentStatus;
import com.example.medhub.entity.AppointmentType;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.mapper.AppointmentsMapper;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentsDto(Long appointmentId, UserDto user, DoctorDto doctor, LocalDate date, LocalTime time,
                              LocationDto location, AppointmentStatus appointmentStatus,
                              AppointmentType appointmentType) {
    public static AppointmentsDto from(AppointmentsEntity appointment) {
        return AppointmentsMapper.APPOINTMENTS_MAPPER.toAppointmentDto(appointment);
    }
}
