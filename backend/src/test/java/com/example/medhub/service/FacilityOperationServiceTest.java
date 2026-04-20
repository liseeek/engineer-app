package com.example.medhub.service;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.exceptions.UnauthorizedOperationException;
import com.example.medhub.repository.AppointmentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityOperationServiceTest {

    @Mock
    private AppointmentsRepository appointmentsRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private AppointmentSlotService appointmentSlotService;

    @InjectMocks
    private FacilityOperationService facilityOperationService;

    @Test
    void cancelAppointment_success_asWorker_createsFreeSlotClone() {
        Worker worker = workerAtLocation(10L);
        Patient patient = new Patient();
        patient.setUserId(1L);

        AppointmentsEntity appointment = createAppointment(
                1L, patient, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);

        when(securityService.getCurrentWorker()).thenReturn(worker);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentsRepository.saveAndFlush(any(AppointmentsEntity.class))).thenReturn(appointment);

        facilityOperationService.cancelAppointment(1L, "Patient asked to cancel");

        verify(appointmentsRepository).saveAndFlush(appointment);
        verify(appointmentSlotService).releaseSlotToPool(appointment);
    }

    @Test
    void rescheduleAppointment_success_asWorker() {
        Worker worker = workerAtLocation(10L);
        Patient patient = new Patient();
        patient.setUserId(1L);

        AppointmentsEntity oldAppointment = createAppointment(
                1L, patient, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, null, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 11L, 10L);

        when(securityService.getCurrentWorker()).thenReturn(worker);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));
        when(appointmentsRepository.findWithLockingById(2L)).thenReturn(Optional.of(newSlot));

        facilityOperationService.rescheduleAppointment(1L, 2L, "Doctor unavailable at original time");

        assertNull(oldAppointment.getPatient());
        assertEquals(AppointmentStatus.ACTIVE, oldAppointment.getAppointmentStatus());
        assertNull(oldAppointment.getRescheduleReason());

        assertEquals(patient.getUserId(), newSlot.getPatient().getUserId());
        assertEquals(AppointmentStatus.RESCHEDULED, newSlot.getAppointmentStatus());
        assertEquals("Doctor unavailable at original time", newSlot.getRescheduleReason());

        verify(appointmentsRepository).save(oldAppointment);
        verify(appointmentsRepository).save(newSlot);
        verify(appointmentSlotService).validateRescheduleEligible(oldAppointment);
        verify(appointmentSlotService).validateNewSlot(newSlot, oldAppointment);
    }

    @Test
    void rescheduleAppointment_workerWrongFacility_throwsUnauthorizedOperationException() {
        Worker worker = workerAtLocation(99L);
        Patient patient = new Patient();
        patient.setUserId(1L);

        AppointmentsEntity oldAppointment = createAppointment(
                1L, patient, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);

        when(securityService.getCurrentWorker()).thenReturn(worker);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));

        assertThrows(UnauthorizedOperationException.class,
                () -> facilityOperationService.rescheduleAppointment(1L, 2L, "Reason"));
        verify(appointmentsRepository, never()).findWithLockingById(any());
        verify(appointmentsRepository, never()).save(any());
    }

    @Test
    void rescheduleAppointment_slotDifferentDoctor_throwsMedHubServiceException() {
        Worker worker = workerAtLocation(10L);
        Patient patient = new Patient();
        patient.setUserId(1L);

        AppointmentsEntity oldAppointment = createAppointment(
                1L, patient, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, null, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 12L, 10L);

        when(securityService.getCurrentWorker()).thenReturn(worker);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));
        when(appointmentsRepository.findWithLockingById(2L)).thenReturn(Optional.of(newSlot));
        doThrow(new MedHubServiceException("New slot must belong to the same doctor."))
                .when(appointmentSlotService).validateNewSlot(newSlot, oldAppointment);

        assertThrows(MedHubServiceException.class,
                () -> facilityOperationService.rescheduleAppointment(1L, 2L, "Reason"));
        verify(appointmentsRepository, never()).save(any());
    }

    private Worker workerAtLocation(Long locationId) {
        LocationEntity location = new LocationEntity();
        location.setLocationId(locationId);
        Worker worker = new Worker();
        worker.setLocation(location);
        return worker;
    }

    private AppointmentsEntity createAppointment(
            Long id,
            Patient patient,
            AppointmentStatus status,
            LocalDate date,
            Long doctorId,
            Long locationId) {
        Doctor doctor = new Doctor();
        doctor.setUserId(doctorId);
        LocationEntity location = new LocationEntity();
        location.setLocationId(locationId);

        AppointmentsEntity appointment = new AppointmentsEntity();
        appointment.setAppointmentId(id);
        appointment.setPatient(patient);
        appointment.setAppointmentStatus(status);
        appointment.setDate(date);
        appointment.setDoctor(doctor);
        appointment.setLocation(location);
        return appointment;
    }
}
