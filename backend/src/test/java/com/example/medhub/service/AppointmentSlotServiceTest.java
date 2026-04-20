package com.example.medhub.service;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.AppointmentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentSlotServiceTest {

    @Mock
    private AppointmentsRepository appointmentsRepository;

    @InjectMocks
    private AppointmentSlotService appointmentSlotService;

    @Test
    void releaseSlotToPool_createsFreeActiveClone() {
        AppointmentsEntity canceledAppointment = createAppointment(
                1L, new Patient(), AppointmentStatus.CANCELED, LocalDate.now().plusDays(1), 11L, 10L);

        appointmentSlotService.releaseSlotToPool(canceledAppointment);

        ArgumentCaptor<AppointmentsEntity> captor = ArgumentCaptor.forClass(AppointmentsEntity.class);
        verify(appointmentsRepository).save(captor.capture());
        AppointmentsEntity freeSlot = captor.getValue();

        assertNull(freeSlot.getPatient());
        assertEquals(AppointmentStatus.ACTIVE, freeSlot.getAppointmentStatus());
        assertEquals(canceledAppointment.getDoctor().getUserId(), freeSlot.getDoctor().getUserId());
        assertEquals(canceledAppointment.getLocation().getLocationId(), freeSlot.getLocation().getLocationId());
        assertEquals(canceledAppointment.getDate(), freeSlot.getDate());
        assertEquals(canceledAppointment.getTime(), freeSlot.getTime());
        assertEquals(canceledAppointment.getAppointmentType(), freeSlot.getAppointmentType());
    }

    @Test
    void validateRescheduleEligible_validAppointment_doesNotThrow() {
        AppointmentsEntity appointment = createAppointment(
                1L, new Patient(), AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);

        appointmentSlotService.validateRescheduleEligible(appointment);
    }

    @Test
    void validateRescheduleEligible_canceledAppointment_throws() {
        AppointmentsEntity appointment = createAppointment(
                1L, new Patient(), AppointmentStatus.CANCELED, LocalDate.now().plusDays(1), 11L, 10L);

        assertThrows(MedHubServiceException.class,
                () -> appointmentSlotService.validateRescheduleEligible(appointment));
    }

    @Test
    void validateRescheduleEligible_pastAppointment_throws() {
        AppointmentsEntity appointment = createAppointment(
                1L, new Patient(), AppointmentStatus.ACTIVE, LocalDate.now().minusDays(1), 11L, 10L);

        assertThrows(MedHubServiceException.class,
                () -> appointmentSlotService.validateRescheduleEligible(appointment));
    }

    @Test
    void validateNewSlot_validSlot_doesNotThrow() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, new Patient(), AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, null, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 11L, 10L);

        appointmentSlotService.validateNewSlot(newSlot, oldAppointment);
    }

    @Test
    void validateNewSlot_takenSlot_throws() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, new Patient(), AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, new Patient(), AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 11L, 10L);

        assertThrows(MedHubServiceException.class,
                () -> appointmentSlotService.validateNewSlot(newSlot, oldAppointment));
    }

    @Test
    void validateNewSlot_differentDoctor_throws() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, new Patient(), AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, null, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 22L, 10L);

        assertThrows(MedHubServiceException.class,
                () -> appointmentSlotService.validateNewSlot(newSlot, oldAppointment));
    }

    @Test
    void validateNewSlot_differentLocation_throws() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, new Patient(), AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, null, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 11L, 99L);

        assertThrows(MedHubServiceException.class,
                () -> appointmentSlotService.validateNewSlot(newSlot, oldAppointment));
    }

    @Test
    void validateRescheduleEligible_todayPastTime_throws() {
        AppointmentsEntity appointment = createAppointment(
                1L, new Patient(), AppointmentStatus.ACTIVE,
                LocalDate.now(), LocalTime.now().minusMinutes(1), 11L, 10L);

        assertThrows(MedHubServiceException.class,
                () -> appointmentSlotService.validateRescheduleEligible(appointment));
    }

    @Test
    void validateRescheduleEligible_todayFutureTime_doesNotThrow() {
        AppointmentsEntity appointment = createAppointment(
                1L, new Patient(), AppointmentStatus.ACTIVE,
                LocalDate.now(), LocalTime.now().plusHours(1), 11L, 10L);

        appointmentSlotService.validateRescheduleEligible(appointment);
    }

    @Test
    void validateNewSlot_todayPastTime_throws() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, new Patient(), AppointmentStatus.ACTIVE,
                LocalDate.now().plusDays(1), LocalTime.of(12, 0), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, null, AppointmentStatus.ACTIVE,
                LocalDate.now(), LocalTime.now().minusMinutes(1), 11L, 10L);

        assertThrows(MedHubServiceException.class,
                () -> appointmentSlotService.validateNewSlot(newSlot, oldAppointment));
    }

    private AppointmentsEntity createAppointment(
            Long id,
            Patient patient,
            AppointmentStatus status,
            LocalDate date,
            Long doctorId,
            Long locationId) {
        return createAppointment(id, patient, status, date, LocalTime.of(12, 0), doctorId, locationId);
    }

    private AppointmentsEntity createAppointment(
            Long id,
            Patient patient,
            AppointmentStatus status,
            LocalDate date,
            LocalTime time,
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
        appointment.setTime(time);
        appointment.setDoctor(doctor);
        appointment.setLocation(location);
        return appointment;
    }
}
