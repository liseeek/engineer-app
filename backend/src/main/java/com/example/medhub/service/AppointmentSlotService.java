package com.example.medhub.service;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.AppointmentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {

    private final AppointmentsRepository appointmentsRepository;

    public void releaseSlotToPool(AppointmentsEntity canceledAppointment) {
        AppointmentsEntity freeSlot = AppointmentsEntity.builder()
                .doctor(canceledAppointment.getDoctor())
                .location(canceledAppointment.getLocation())
                .date(canceledAppointment.getDate())
                .time(canceledAppointment.getTime())
                .appointmentType(canceledAppointment.getAppointmentType())
                .appointmentStatus(AppointmentStatus.ACTIVE)
                .build();
        appointmentsRepository.save(freeSlot);
    }

    public void validateRescheduleEligible(AppointmentsEntity appointment) {
        if (appointment.getAppointmentStatus() != AppointmentStatus.ACTIVE
                && appointment.getAppointmentStatus() != AppointmentStatus.RESCHEDULED) {
            throw new MedHubServiceException("Only active appointments can be rescheduled.");
        }
        if (isInPast(appointment.getDate(), appointment.getTime())) {
            throw new MedHubServiceException("Past appointments cannot be rescheduled.");
        }
    }

    public void validateNewSlot(AppointmentsEntity newSlot, AppointmentsEntity oldAppointment) {
        if (newSlot.getPatient() != null) {
            throw new MedHubServiceException("Selected slot is already assigned.");
        }
        if (isInPast(newSlot.getDate(), newSlot.getTime())) {
            throw new MedHubServiceException("Selected slot is in the past.");
        }
        if (newSlot.getAppointmentStatus() != AppointmentStatus.ACTIVE
                && newSlot.getAppointmentStatus() != AppointmentStatus.RESCHEDULED) {
            throw new MedHubServiceException("Selected slot is not available for reschedule.");
        }
        if (!newSlot.getDoctor().getUserId().equals(oldAppointment.getDoctor().getUserId())) {
            throw new MedHubServiceException("New slot must belong to the same doctor.");
        }
        if (!newSlot.getLocation().getLocationId().equals(oldAppointment.getLocation().getLocationId())) {
            throw new MedHubServiceException("New slot must belong to the same location.");
        }
    }

    private static boolean isInPast(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return true;
        }
        return !LocalDateTime.of(date, time).isAfter(LocalDateTime.now());
    }
}
