package com.example.medhub.service;

import com.example.medhub.configuration.MedHubProperties;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.entity.Admin;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.Worker;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.exceptions.UnauthorizedOperationException;
import com.example.medhub.repository.AppointmentsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentsServiceTest {

    @Mock
    private AppointmentsRepository appointmentsRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private MedHubProperties medHubProperties;

    @Mock
    private AppointmentSlotService appointmentSlotService;

    @InjectMocks
    private AppointmentsService appointmentsService;

    private Patient testUser;
    private AppointmentsEntity testAppointment;

    @BeforeEach
    void setUp() {
        testUser = new Patient();
        testUser.setUserId(1L);
        testUser.setEmail("test@user.com");

        testAppointment = new AppointmentsEntity();
        testAppointment.setAppointmentId(1L);
        testAppointment.setAppointmentStatus(AppointmentStatus.ACTIVE);
        testAppointment.setPatient(null);
        testAppointment.setDate(LocalDate.now().plusDays(1));
        testAppointment.setTime(LocalTime.of(12, 0));

        MedHubProperties.Appointments appts = new MedHubProperties.Appointments();
        appts.setMaxUpcomingPerPatient(5);
        when(medHubProperties.getAppointments()).thenReturn(appts);
    }

    @Test
    void addAppointmentToUser_success() {
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.countUpcomingForPatient(
                eq(1L), any(LocalDate.class), any(LocalTime.class),
                eq(List.of(AppointmentStatus.ACTIVE.name(), AppointmentStatus.RESCHEDULED.name()))))
                .thenReturn(0L);
        when(appointmentsRepository.findWithLockingById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentsRepository.save(any(AppointmentsEntity.class))).thenReturn(testAppointment);

        appointmentsService.addAppointmentToUser(1L);

        assertNotNull(testAppointment.getPatient());
        assertEquals(testUser.getUserId(), testAppointment.getPatient().getUserId());
        verify(appointmentsRepository, times(1)).save(testAppointment);
    }

    @Test
    void addAppointmentToUser_appointmentNotFound_throwsMedHubServiceException() {
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.countUpcomingForPatient(
                eq(1L), any(LocalDate.class), any(LocalTime.class),
                eq(List.of(AppointmentStatus.ACTIVE.name(), AppointmentStatus.RESCHEDULED.name()))))
                .thenReturn(0L);
        when(appointmentsRepository.findWithLockingById(1L)).thenReturn(Optional.empty());

        assertThrows(MedHubServiceException.class, () -> appointmentsService.addAppointmentToUser(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void addAppointmentToUser_appointmentAlreadyTaken_throwsMedHubServiceException() {
        testAppointment.setPatient(new Patient());
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.countUpcomingForPatient(
                eq(1L), any(LocalDate.class), any(LocalTime.class),
                eq(List.of(AppointmentStatus.ACTIVE.name(), AppointmentStatus.RESCHEDULED.name()))))
                .thenReturn(0L);
        when(appointmentsRepository.findWithLockingById(1L)).thenReturn(Optional.of(testAppointment));

        assertThrows(MedHubServiceException.class, () -> appointmentsService.addAppointmentToUser(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void addAppointmentToUser_maxUpcomingReached_throwsMedHubServiceException() {
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.countUpcomingForPatient(
                eq(1L), any(LocalDate.class), any(LocalTime.class),
                eq(List.of(AppointmentStatus.ACTIVE.name(), AppointmentStatus.RESCHEDULED.name()))))
                .thenReturn(5L);

        assertThrows(MedHubServiceException.class, () -> appointmentsService.addAppointmentToUser(1L));
        verify(appointmentsRepository, never()).findWithLockingById(anyLong());
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void addAppointmentToUser_pastSlot_throwsMedHubServiceException() {
        testAppointment.setDate(LocalDate.now().minusDays(1));
        testAppointment.setTime(LocalTime.of(12, 0));
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.countUpcomingForPatient(
                eq(1L), any(LocalDate.class), any(LocalTime.class),
                eq(List.of(AppointmentStatus.ACTIVE.name(), AppointmentStatus.RESCHEDULED.name()))))
                .thenReturn(0L);
        when(appointmentsRepository.findWithLockingById(1L)).thenReturn(Optional.of(testAppointment));

        assertThrows(MedHubServiceException.class, () -> appointmentsService.addAppointmentToUser(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void addAppointmentToUser_todayPastTimeSlot_throwsMedHubServiceException() {
        testAppointment.setDate(LocalDate.now());
        testAppointment.setTime(LocalTime.now().minusMinutes(1));
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.countUpcomingForPatient(
                eq(1L), any(LocalDate.class), any(LocalTime.class),
                eq(List.of(AppointmentStatus.ACTIVE.name(), AppointmentStatus.RESCHEDULED.name()))))
                .thenReturn(0L);
        when(appointmentsRepository.findWithLockingById(1L)).thenReturn(Optional.of(testAppointment));

        assertThrows(MedHubServiceException.class, () -> appointmentsService.addAppointmentToUser(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void completeAppointment_success_asWorker_sameFacility() {
        LocationEntity location = new LocationEntity();
        location.setLocationId(10L);
        testAppointment.setLocation(location);

        LocationEntity workerLocation = new LocationEntity();
        workerLocation.setLocationId(10L);
        Worker worker = new Worker();
        worker.setLocation(workerLocation);

        when(securityService.getCurrentUser()).thenReturn(worker);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentsRepository.save(any(AppointmentsEntity.class))).thenReturn(testAppointment);

        appointmentsService.completeAppointment(1L);

        assertEquals(AppointmentStatus.COMPLETED, testAppointment.getAppointmentStatus());
        verify(appointmentsRepository, times(1)).save(testAppointment);
    }

    @Test
    void completeAppointment_success_asAdmin() {
        Admin admin = new Admin();
        when(securityService.getCurrentUser()).thenReturn(admin);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentsRepository.save(any(AppointmentsEntity.class))).thenReturn(testAppointment);

        appointmentsService.completeAppointment(1L);

        assertEquals(AppointmentStatus.COMPLETED, testAppointment.getAppointmentStatus());
        verify(appointmentsRepository, times(1)).save(testAppointment);
    }

    @Test
    void completeAppointment_workerWrongFacility_throwsUnauthorizedOperationException() {
        LocationEntity appointmentLoc = new LocationEntity();
        appointmentLoc.setLocationId(10L);
        testAppointment.setLocation(appointmentLoc);

        LocationEntity workerLoc = new LocationEntity();
        workerLoc.setLocationId(99L);
        Worker worker = new Worker();
        worker.setLocation(workerLoc);

        when(securityService.getCurrentUser()).thenReturn(worker);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        assertThrows(UnauthorizedOperationException.class, () -> appointmentsService.completeAppointment(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void completeAppointment_appointmentNotFound_throwsEntityNotFoundException() {
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> appointmentsService.completeAppointment(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void cancelAppointment_success() {
        testAppointment.setPatient(testUser);
        Doctor doctor = new Doctor();
        doctor.setUserId(11L);
        LocationEntity location = new LocationEntity();
        location.setLocationId(10L);
        testAppointment.setDoctor(doctor);
        testAppointment.setLocation(location);
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentsRepository.saveAndFlush(any(AppointmentsEntity.class))).thenReturn(testAppointment);

        appointmentsService.cancelAppointment(1L);

        assertEquals(AppointmentStatus.CANCELED, testAppointment.getAppointmentStatus());
        verify(appointmentsRepository).saveAndFlush(testAppointment);
        verify(appointmentSlotService).releaseSlotToPool(testAppointment);
    }

    @Test
    void cancelAppointment_notOwnAppointment_throwsUnauthorizedOperationException() {
        Patient otherPatient = new Patient();
        otherPatient.setUserId(99L);
        testAppointment.setPatient(otherPatient);
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        assertThrows(UnauthorizedOperationException.class, () -> appointmentsService.cancelAppointment(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
        verify(appointmentsRepository, never()).saveAndFlush(any(AppointmentsEntity.class));
    }

    @Test
    void cancelAppointment_appointmentNotFound_throwsEntityNotFoundException() {
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> appointmentsService.cancelAppointment(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
        verify(appointmentsRepository, never()).saveAndFlush(any(AppointmentsEntity.class));
    }

    @Test
    void rescheduleAppointment_success() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, testUser, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, null, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 11L, 10L);

        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));
        when(appointmentsRepository.findWithLockingById(2L)).thenReturn(Optional.of(newSlot));

        appointmentsService.rescheduleAppointment(1L, 2L);

        assertNull(oldAppointment.getPatient());
        assertEquals(AppointmentStatus.ACTIVE, oldAppointment.getAppointmentStatus());
        assertNull(oldAppointment.getRescheduleReason());

        assertEquals(testUser.getUserId(), newSlot.getPatient().getUserId());
        assertEquals(AppointmentStatus.RESCHEDULED, newSlot.getAppointmentStatus());
        assertNull(newSlot.getRescheduleReason());

        verify(appointmentsRepository).save(oldAppointment);
        verify(appointmentsRepository).save(newSlot);
        verify(appointmentSlotService).validateRescheduleEligible(oldAppointment);
        verify(appointmentSlotService).validateNewSlot(newSlot, oldAppointment);
    }

    @Test
    void rescheduleAppointment_notOwnAppointment_throwsUnauthorizedOperationException() {
        Patient otherPatient = new Patient();
        otherPatient.setUserId(99L);
        AppointmentsEntity oldAppointment = createAppointment(
                1L, otherPatient, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);

        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));

        assertThrows(UnauthorizedOperationException.class, () -> appointmentsService.rescheduleAppointment(1L, 2L));
        verify(appointmentsRepository, never()).findWithLockingById(anyLong());
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void rescheduleAppointment_oldAppointmentCanceled_throwsMedHubServiceException() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, testUser, AppointmentStatus.CANCELED, LocalDate.now().plusDays(1), 11L, 10L);

        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));
        doThrow(new MedHubServiceException("Only active appointments can be rescheduled."))
                .when(appointmentSlotService).validateRescheduleEligible(oldAppointment);

        assertThrows(MedHubServiceException.class, () -> appointmentsService.rescheduleAppointment(1L, 2L));
        verify(appointmentsRepository, never()).findWithLockingById(anyLong());
    }

    @Test
    void rescheduleAppointment_oldAppointmentInPast_throwsMedHubServiceException() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, testUser, AppointmentStatus.ACTIVE, LocalDate.now().minusDays(1), 11L, 10L);

        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));
        doThrow(new MedHubServiceException("Past appointments cannot be rescheduled."))
                .when(appointmentSlotService).validateRescheduleEligible(oldAppointment);

        assertThrows(MedHubServiceException.class, () -> appointmentsService.rescheduleAppointment(1L, 2L));
        verify(appointmentsRepository, never()).findWithLockingById(anyLong());
    }

    @Test
    void rescheduleAppointment_newSlotAlreadyTaken_throwsMedHubServiceException() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, testUser, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, new Patient(), AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 11L, 10L);

        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));
        when(appointmentsRepository.findWithLockingById(2L)).thenReturn(Optional.of(newSlot));
        doThrow(new MedHubServiceException("Selected slot is already assigned."))
                .when(appointmentSlotService).validateNewSlot(newSlot, oldAppointment);

        assertThrows(MedHubServiceException.class, () -> appointmentsService.rescheduleAppointment(1L, 2L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void rescheduleAppointment_differentDoctor_throwsMedHubServiceException() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, testUser, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, null, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 12L, 10L);

        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));
        when(appointmentsRepository.findWithLockingById(2L)).thenReturn(Optional.of(newSlot));
        doThrow(new MedHubServiceException("New slot must belong to the same doctor."))
                .when(appointmentSlotService).validateNewSlot(newSlot, oldAppointment);

        assertThrows(MedHubServiceException.class, () -> appointmentsService.rescheduleAppointment(1L, 2L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void rescheduleAppointment_differentLocation_throwsMedHubServiceException() {
        AppointmentsEntity oldAppointment = createAppointment(
                1L, testUser, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(1), 11L, 10L);
        AppointmentsEntity newSlot = createAppointment(
                2L, null, AppointmentStatus.ACTIVE, LocalDate.now().plusDays(2), 11L, 99L);

        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(oldAppointment));
        when(appointmentsRepository.findWithLockingById(2L)).thenReturn(Optional.of(newSlot));
        doThrow(new MedHubServiceException("New slot must belong to the same location."))
                .when(appointmentSlotService).validateNewSlot(newSlot, oldAppointment);

        assertThrows(MedHubServiceException.class, () -> appointmentsService.rescheduleAppointment(1L, 2L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
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
