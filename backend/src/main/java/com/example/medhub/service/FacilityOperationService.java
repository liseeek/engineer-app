package com.example.medhub.service;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.exceptions.UnauthorizedOperationException;
import com.example.medhub.repository.AppointmentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medhub.aspect.annotation.Auditable;

@Service
@RequiredArgsConstructor
public class FacilityOperationService {

    private final AppointmentsRepository appointmentsRepository;
    private final SecurityService securityService;
    private final AppointmentSlotService appointmentSlotService;

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

        appointmentsRepository.saveAndFlush(appointment);
        appointmentSlotService.releaseSlotToPool(appointment);
    }

    @Transactional
    @Auditable(action = "RESCHEDULE_APPOINTMENT", resourceId = "#oldAppointmentId")
    public void rescheduleAppointment(Long oldAppointmentId, Long newSlotId, String reason) {
        Worker currentWorker = securityService.getCurrentWorker();
        AppointmentsEntity oldAppointment = getAppointmentOrThrow(oldAppointmentId);

        validateWorkerAccess(currentWorker, oldAppointment);
        if (oldAppointment.getPatient() == null) {
            throw new MedHubServiceException("Only booked appointments can be rescheduled.");
        }
        appointmentSlotService.validateRescheduleEligible(oldAppointment);

        AppointmentsEntity newSlot = appointmentsRepository.findWithLockingById(newSlotId)
                .orElseThrow(() -> new MedHubServiceException("New time slot not found"));
        appointmentSlotService.validateNewSlot(newSlot, oldAppointment);

        Patient patient = oldAppointment.getPatient();
        oldAppointment.setPatient(null);
        oldAppointment.setAppointmentStatus(AppointmentStatus.ACTIVE);
        oldAppointment.setRescheduleReason(null);

        newSlot.setPatient(patient);
        newSlot.setAppointmentStatus(AppointmentStatus.RESCHEDULED);
        newSlot.setRescheduleReason(reason);

        appointmentsRepository.save(oldAppointment);
        appointmentsRepository.save(newSlot);
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
