package com.example.medhub.service;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.User;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.exceptions.UnauthorizedOperationException;
import com.example.medhub.repository.AppointmentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.example.medhub.aspect.annotation.Auditable;

@Service
@RequiredArgsConstructor
public class FacilityOperationService {

    private final AppointmentsRepository appointmentsRepository;
    private final SecurityService securityService;
    private final AvailabilityService availabilityService;

    @Transactional
    @Auditable(action = "CANCEL_APPOINTMENT", resourceId = "#appointmentId")
    public void cancelAppointment(Long appointmentId, String reason) {
        Worker currentWorker = securityService.getCurrentWorker();
        AppointmentsEntity appointment = getAppointmentOrThrow(appointmentId);

        validateWorkerAccess(currentWorker, appointment);

        if (appointment.getAppointmentStatus() == AppointmentStatus.CANCELED
                || appointment.getAppointmentStatus() == AppointmentStatus.COMPLETED) {
            throw new MedHubServiceException("Cannot cancel an appointment that is already processed.");
        }

        appointment.setAppointmentStatus(AppointmentStatus.CANCELED);

        appointmentsRepository.save(appointment);
    }

    @Transactional
    public void rescheduleAppointment(Long appointmentId, LocalDateTime newStart) {
        Worker currentWorker = securityService.getCurrentWorker();
        AppointmentsEntity appointment = getAppointmentOrThrow(appointmentId);

        validateWorkerAccess(currentWorker, appointment);

        validateWorkerAccess(currentWorker, appointment);

        appointment.setDate(newStart.toLocalDate());
        appointment.setTime(newStart.toLocalTime());

        appointment.setAppointmentStatus(AppointmentStatus.RESCHEDULED);

        appointmentsRepository.save(appointment);
    }

    private AppointmentsEntity getAppointmentOrThrow(Long id) {
        return appointmentsRepository.findById(id)
                .orElseThrow(() -> new MedHubServiceException("Appointment not found"));
    }

    private void validateWorkerAccess(Worker worker, AppointmentsEntity appointment) {
        if (!worker.getLocation().getLocationId().equals(appointment.getLocation().getLocationId())) {
            throw new UnauthorizedOperationException(
                    "Worker does not belong to the facility where the appointment is scheduled.");
        }
    }
}
