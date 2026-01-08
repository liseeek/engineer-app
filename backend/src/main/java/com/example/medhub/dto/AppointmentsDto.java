package com.example.medhub.dto;

import com.example.medhub.entity.AppointmentStatus;
import com.example.medhub.entity.AppointmentType;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentsDto(Long appointmentId, UserDto user, DoctorDto doctor, LocalDate date, LocalTime time,
                              LocationDto location, AppointmentStatus appointmentStatus,
                              AppointmentType appointmentType) {
}
