package com.example.medhub.service;

import com.example.medhub.configuration.MedHubProperties;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.entity.Admin;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.User;
import com.example.medhub.entity.Worker;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.exceptions.UnauthorizedOperationException;
import com.example.medhub.repository.AppointmentsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentsService {
    private final AppointmentsRepository appointmentsRepository;
    private final SecurityService securityService;
    private final MedHubProperties medHubProperties;
    private final AppointmentSlotService appointmentSlotService;

    @Transactional
    public void addAppointmentToUser(Long appointmentId) {
        Patient patient = securityService.getCurrentPatient();
        int maxUpcoming = medHubProperties.getAppointments().getMaxUpcomingPerPatient();
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        long upcomingCount = appointmentsRepository.countUpcomingForPatient(
                patient.getUserId(),
                today,
                nowTime,
                List.of(AppointmentStatus.ACTIVE.name(), AppointmentStatus.RESCHEDULED.name()));
        if (upcomingCount >= maxUpcoming) {
            throw new MedHubServiceException(
                    "You have reached the maximum number of upcoming appointments (" + maxUpcoming
                            + "). Cancel or complete an existing visit before booking another.");
        }
        AppointmentsEntity appointment = appointmentsRepository.findWithLockingById(appointmentId)
                .orElseThrow(() -> new MedHubServiceException("Not found"));
        if (appointment.getPatient() != null) {
            throw new MedHubServiceException("Availability already assigned");
        }
        if (appointment.getDate() == null || appointment.getTime() == null
                || !LocalDateTime.of(appointment.getDate(), appointment.getTime()).isAfter(LocalDateTime.now())) {
            throw new MedHubServiceException("Selected slot is in the past.");
        }
        appointment.setPatient(patient);
        appointmentsRepository.save(appointment);
    }

    @Transactional
    public void completeAppointment(Long appointmentId) {
        AppointmentsEntity appointment = appointmentsRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        User user = securityService.getCurrentUser();
        if (user instanceof Worker worker) {
            if (!worker.getLocation().getLocationId().equals(appointment.getLocation().getLocationId())) {
                throw new UnauthorizedOperationException(
                        "Worker does not belong to the facility where the appointment is scheduled.");
            }
        } else if (!(user instanceof Admin)) {
            throw new UnauthorizedOperationException("Only workers and administrators can complete appointments.");
        }
        appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);
        appointmentsRepository.save(appointment);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Patient patient = securityService.getCurrentPatient();
        AppointmentsEntity appointment = appointmentsRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        if (appointment.getPatient() == null
                || !appointment.getPatient().getUserId().equals(patient.getUserId())) {
            throw new UnauthorizedOperationException("You can only cancel your own appointments.");
        }
        appointment.setAppointmentStatus(AppointmentStatus.CANCELED);
        appointmentsRepository.saveAndFlush(appointment);
        appointmentSlotService.releaseSlotToPool(appointment);
    }

    @Transactional
    public void rescheduleAppointment(Long oldAppointmentId, Long newSlotId) {
        Patient patient = securityService.getCurrentPatient();

        AppointmentsEntity oldAppointment = appointmentsRepository.findById(oldAppointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        if (oldAppointment.getPatient() == null
                || !oldAppointment.getPatient().getUserId().equals(patient.getUserId())) {
            throw new UnauthorizedOperationException("You can only reschedule your own appointments.");
        }
        appointmentSlotService.validateRescheduleEligible(oldAppointment);

        AppointmentsEntity newSlot = appointmentsRepository.findWithLockingById(newSlotId)
                .orElseThrow(() -> new MedHubServiceException("New time slot not found"));
        appointmentSlotService.validateNewSlot(newSlot, oldAppointment);

        oldAppointment.setPatient(null);
        oldAppointment.setAppointmentStatus(AppointmentStatus.ACTIVE);
        oldAppointment.setRescheduleReason(null);

        newSlot.setPatient(patient);
        newSlot.setAppointmentStatus(AppointmentStatus.RESCHEDULED);
        newSlot.setRescheduleReason(null);

        appointmentsRepository.save(oldAppointment);
        appointmentsRepository.save(newSlot);
    }
}
